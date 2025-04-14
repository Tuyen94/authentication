package tuyenbd.authentication.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.controller.dto.UserUpdateRequest;
import tuyenbd.authentication.domain.user.entity.User;
import tuyenbd.authentication.domain.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(
            new User(),
            new User()
        );
        when(userService.getAllUsers()).thenReturn(expectedUsers);

        // When
        ResponseEntity<List<User>> response = controller.getAllUsers();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedUsers, response.getBody());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        User expectedUser = new User();
        when(userService.getUserById(userId)).thenReturn(expectedUser);

        // When
        ResponseEntity<User> response = controller.getUserById(userId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedUser, response.getBody());
        verify(userService).getUserById(userId);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        User updatedUser = new User();
        when(userService.updateUser(userId, request)).thenReturn(updatedUser);

        // When
        ResponseEntity<User> response = controller.updateUser(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateUser(userId, request);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        // Given
        RegisterRequest request = new RegisterRequest();
        User createdUser = new User();
        when(userService.createUser(request)).thenReturn(createdUser);

        // When
        ResponseEntity<User> response = controller.createUser(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(createdUser, response.getBody());
        verify(userService).createUser(request);
    }

    @Test
    void deleteUser_ShouldReturnOkResponse() {
        // Given
        Long userId = 1L;

        // When
        ResponseEntity<Void> response = controller.deleteUser(userId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(userService).inactiveUser(userId);
    }
}