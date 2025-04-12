package tuyenbd.authentication.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tuyenbd.authentication.domain.auth.entity.Token;
import tuyenbd.authentication.domain.auth.enums.TokenType;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = "select * from tokens t where t.user_id = :userId and t.status = 'ACTIVE'", nativeQuery = true)
    List<Token> findAllActiveTokensByUser(@Param("userId") Long userId);

    Optional<Token> findByToken(String token);

    Optional<Token> findByTokenAndTokenType(String jwt, TokenType tokenType);
}