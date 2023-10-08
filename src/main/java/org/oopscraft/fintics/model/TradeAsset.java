package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.TradeAssetEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAsset extends Asset {

    private String tradeId;

    private boolean enabled;

    private BigDecimal tradeRatio;

    private BigDecimal limitRatio;

    public static TradeAsset from(TradeAssetEntity tradeAssetEntity) {
        return TradeAsset.builder()
                .tradeId(tradeAssetEntity.getTradeId())
                .symbol(tradeAssetEntity.getSymbol())
                .name(tradeAssetEntity.getName())
                .type(tradeAssetEntity.getType())
                .enabled(tradeAssetEntity.isEnabled())
                .tradeRatio(tradeAssetEntity.getTradeRatio())
                .limitRatio(tradeAssetEntity.getLimitRatio())
                .build();
    }

}
