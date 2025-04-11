package tuyenbd.authentication.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuyenbd.authentication.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}