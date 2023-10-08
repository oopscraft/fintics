package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanToYNConverter;
import org.oopscraft.fintics.model.AssetType;

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
public class TradeAssetEntity extends SystemFieldEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private String symbol;
    }

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "type", length = 16)
    AssetType type;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private boolean enabled;

    @Column(name = "trade_ratio")
    private BigDecimal tradeRatio;

    @Column(name = "limit_ratio")
    private BigDecimal limitRatio;

}
