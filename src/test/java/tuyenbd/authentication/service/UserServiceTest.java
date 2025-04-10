package tuyenbd.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tuyenbd.authentication.dto.UserUpdateRequest;
import tuyenbd.authentication.entity.User;
import tuyenbd.authentication.repository.TokenRepository;
import tuyenbd.authentication.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUserFields() {
        // Arrange
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .build();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User updatedUser = userService.updateUser(userId, updateRequest);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("Jane", updatedUser.getFirstname());
        assertEquals("Smith", updatedUser.getLastname());
        assertEquals("jane@example.com", updatedUser.getEmail());
    }
}