package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fintics_basket")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketEntity extends BaseEntity {

    @Id
    @Column(name = "basket_id")
    private String basketId;

    @Column(name = "basket_name")
    private String basketName;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "basket_id", updatable = false)
    @OrderBy(BasketAssetEntity_.SORT)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private List<BasketAssetEntity> basketAssets = new ArrayList<>();

}
