package tuyenbd.authentication.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.controller.dto.UserUpdateRequest;
import tuyenbd.authentication.domain.auth.repository.TokenRepository;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.Role;
import tuyenbd.authentication.domain.user.repository.UserRepository;
import tuyenbd.authentication.domain.user.service.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);
        updateUserFields(user, request);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        revokeUserTokens(id);
        userRepository.delete(user);
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
        return currentUser.getId().equals(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User createUser(RegisterRequest request) {
        validateNewUser(request.getEmail());
        User user = buildNewUser(request);
        return save(user);
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
    }

    private void revokeUserTokens(Long userId) {
        var validTokens = tokenRepository.findAllValidTokensByUser(userId);
        if (!validTokens.isEmpty()) {
            validTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validTokens);
        }
    }

    private void validateNewUser(String email) {
        if (existsByEmail(email)) {
            log.warn("Attempted to create user with existing email: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }
    }

    private User buildNewUser(RegisterRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .role(Role.USER)
                .build();
    }
}