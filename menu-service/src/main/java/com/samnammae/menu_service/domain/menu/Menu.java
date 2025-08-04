package com.samnammae.menu_service.domain.menu;

import com.samnammae.menu_service.domain.menucategory.MenuCategory;
import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id")
    private MenuCategory menuCategory;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private int price;

    @Lob
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean isSoldOut;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_option_category",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "option_category_id")
    )
    private Set<OptionCategory> optionCategories = new HashSet<>();

    public void update(String name, int price, String description, String imageUrl, boolean isSoldOut, MenuCategory menuCategory, Set<OptionCategory> optionCategories) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isSoldOut = isSoldOut;
        this.menuCategory = menuCategory;
        this.optionCategories = optionCategories;
    }
}
