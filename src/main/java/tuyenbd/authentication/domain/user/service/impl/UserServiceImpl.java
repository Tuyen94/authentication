package tuyenbd.authentication.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.controller.dto.UserUpdateRequest;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.UserStatus;
import tuyenbd.authentication.domain.user.repository.UserRepository;
import tuyenbd.authentication.domain.user.service.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.info("Successfully saved user with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());
        return users;
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });
    }

    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", id);
        User user = getUserById(id);
        updateUserFields(user, request);
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user with ID: {}", id);
        return updatedUser;
    }

    @Transactional
    @Override
    public void inactiveUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);
        User user = getUserById(id);
        tokenService.revokeAllUserTokens(user);
        inactiveUser(user);
        log.info("Successfully deactivated user with ID: {}", id);
    }

    private void inactiveUser(User user) {
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
        return currentUser.getId().equals(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found");
                });
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User createUser(RegisterRequest request) {
        log.debug("Creating new user with email: {}", request.getEmail());
        validateNewUser(request.getEmail());
        User user = buildNewUser(request);
        User savedUser = save(user);
        log.info("Successfully created new user with ID: {}", savedUser.getId());
        return savedUser;
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        log.debug("Updating fields for user ID: {}", user.getId());
        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
        log.debug("Field updates completed for user ID: {}", user.getId());
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
                .role(request.getRole())
                .build();
    }
}








