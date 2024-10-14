package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.common.data.BaseEntity;
import org.oopscraft.arch4j.core.common.data.converter.BooleanConverter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fintics_asset")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "name")
    private String name;

    @Column(name = "market", length = 16)
    private String market;

    @Column(name = "exchange", length = 16)
    private String exchange;

    @Column(name = "type", length = 16)
    private String type;

    @Column(name = "market_cap", precision = 32)
    private BigDecimal marketCap;

    @OneToMany
    @JoinColumn(name = "asset_id", referencedColumnName = "asset_id", insertable = false, updatable = false)
    @OrderBy(AssetMetaEntity_.SORT)
    @Builder.Default
    private List<AssetMetaEntity> assetMetaEntities = new ArrayList<>();

}
