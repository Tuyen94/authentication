package tuyenbd.authentication.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tuyenbd.authentication.domain.auth.entity.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
    select t from Token t inner join User u on t.user.id = u.id
    where u.id = :userId and t.status = 'ACTIVE'
    """)
    List<Token> findAllValidTokensByUser(Long userId);

    Optional<Token> findByToken(String token);
}