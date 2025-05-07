package com.samnammae.auth_service.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")  // 테이블명 명시적으로 설정 (예약어 방지)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;
}