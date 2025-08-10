package com.samnammae.menu_service.domain.option;

import com.samnammae.menu_service.domain.optioncategory.OptionCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_category_custom",
                        columnNames = {"option_category_id", "name"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_category_id", nullable = false)
    private OptionCategory optionCategory;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

}