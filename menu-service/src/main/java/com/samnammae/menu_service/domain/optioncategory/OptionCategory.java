package com.samnammae.menu_service.domain.optioncategory;

import com.samnammae.menu_service.domain.option.Option;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "option_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_category_store_custom",
                        columnNames = {"storeId", "name"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_category_id")
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionCategoryType type;

    @Column(nullable = false)
    private boolean isRequired;

    @OneToMany(mappedBy = "optionCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Option> options = new ArrayList<>();

    // update 메서드
    public void update(String name, OptionCategoryType type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }
}