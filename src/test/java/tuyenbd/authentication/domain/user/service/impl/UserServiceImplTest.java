package tuyenbd.authentication.domain.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.controller.dto.UserUpdateRequest;
import tuyenbd.authentication.domain.auth.service.TokenService;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.enums.UserStatus;
import tuyenbd.authentication.domain.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .firstname("John")
                .lastname("Doe")
                .status(UserStatus.ACTIVE)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstname("Jane");
        registerRequest.setLastname("Smith");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstname("Updated");
        updateRequest.setLastname("Name");
    }

    @Test
    void save_ShouldSaveAndReturnUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.save(testUser);

        assertNotNull(savedUser);
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1L, updateRequest);

        assertNotNull(updatedUser);
        assertEquals(updateRequest.getFirstname(), updatedUser.getFirstname());
        assertEquals(updateRequest.getLastname(), updatedUser.getLastname());
    }

    @Test
    void inactiveUser_ShouldUpdateStatusAndRevokeTokens() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.inactiveUser(1L);

        assertEquals(UserStatus.INACTIVE, testUser.getStatus());
        verify(tokenService).revokeAllUserTokens(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void isCurrentUser_WhenUserMatches_ShouldReturnTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        boolean result = userService.isCurrentUser(1L);

        assertTrue(result);
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserByEmail_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
            () -> userService.getUserByEmail("nonexistent@example.com"));
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }

    @Test
    void createUser_WhenEmailNotTaken_ShouldCreateAndReturnUser() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(registerRequest);

        assertNotNull(createdUser);
        assertEquals(registerRequest.getEmail(), createdUser.getEmail());
        assertEquals("encoded_password", createdUser.getPassword());
        assertEquals(registerRequest.getFirstname(), createdUser.getFirstname());
        assertEquals(registerRequest.getLastname(), createdUser.getLastname());
    }

    @Test
    void createUser_WhenEmailTaken_ShouldThrowException() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(registerRequest));
    }
}