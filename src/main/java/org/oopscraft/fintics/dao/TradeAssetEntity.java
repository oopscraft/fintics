package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "fintics_trade_asset")
@IdClass(TradeAssetEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetEntity extends BaseEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private String assetId;
    }

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean enabled;

    @Column(name = "holding_weight")
    private BigDecimal holdingWeight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    private AssetEntity assetEntity;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "trade_id", referencedColumnName = "trade_id"),
            @JoinColumn(name = "asset_id", referencedColumnName = "asset_id")
    })
    private TradeAssetStatusEntity tradeAssetStatusEntity;

}
