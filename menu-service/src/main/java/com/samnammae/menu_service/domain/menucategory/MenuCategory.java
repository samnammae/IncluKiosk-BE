package com.samnammae.menu_service.domain.menucategory;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_category_id")
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int displayOrder;
}