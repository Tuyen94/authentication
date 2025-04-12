package tuyenbd.authentication.domain.user.service;

import tuyenbd.authentication.controller.dto.RegisterRequest;
import tuyenbd.authentication.controller.dto.UserUpdateRequest;
import tuyenbd.authentication.domain.user.entity.User;

import java.util.List;

public interface UserService {
    User save(User user);

    List<User> getAllUsers();

    User getUserById(Long id);

    User updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    boolean isCurrentUser(Long userId);

    User getUserByEmail(String email);

    boolean existsByEmail(String email);

    User createUser(RegisterRequest request);
}