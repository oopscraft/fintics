package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanToYNConverter;

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

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private boolean enabled;

    @Column(name = "hold_ratio")
    private BigDecimal holdRatio;

}
