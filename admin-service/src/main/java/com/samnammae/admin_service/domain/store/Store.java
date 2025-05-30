package com.samnammae.admin_service.domain.store;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;         // 매장 이름

    @Column(nullable = false)
    private String phone;        // 매장 전화번호

    @Column(nullable = false)
    private String address;      // 매장 주소

    @Column(columnDefinition = "TEXT")
    private String introduction; // 매장 소개 (선택)

    private String mainImgUrl;   // 메인 이미지 URL (선택)
    private String logoImgUrl;   // 로고 이미지 URL (선택)
    private String startBackgroundUrl; // 시작 배경 이미지 URL (선택)

    @Column(nullable = false)
    private String mainColor;    // 메인 컬러 (#002F6C 등)

    @Column(nullable = false)
    private String subColor;     // 서브 컬러 (#0051A3 등)

    @Column(nullable = false)
    private String textColor;    // 텍스트 컬러 (#F8F9FA 등)
}
