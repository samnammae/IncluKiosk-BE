package com.samnammae.auth_service.domain.token;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저의 PK (User 엔터티의 id)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    public void updateToken(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
}