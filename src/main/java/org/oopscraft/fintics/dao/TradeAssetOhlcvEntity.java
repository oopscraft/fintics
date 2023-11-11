package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.OhlcvType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_trade_asset_ohlcv")
@IdClass(TradeAssetOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetOhlcvEntity extends OhlcvEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private String symbol;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

}
