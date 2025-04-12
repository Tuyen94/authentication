package tuyenbd.authentication.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tuyenbd.authentication.domain.auth.enums.TokenStatus;
import tuyenbd.authentication.domain.auth.enums.TokenType;
import tuyenbd.authentication.domain.user.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue
    private Long id;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}