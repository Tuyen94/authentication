package tuyenbd.authentication.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuyenbd.authentication.domain.auth.entity.LoginAttempt;
import tuyenbd.authentication.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUserAndTimestampAfter(User user, LocalDateTime timestamp);
    List<LoginAttempt> findByUserOrderByTimestampDesc(User user);
}