package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.TradeAssetEntity;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAsset {

    private String tradeId;

    private String symbol;

    private String name;

    private boolean enabled;

    private BigDecimal holdRatio;

    public static TradeAsset from(TradeAssetEntity tradeAssetEntity) {
        return TradeAsset.builder()
                .tradeId(tradeAssetEntity.getTradeId())
                .symbol(tradeAssetEntity.getSymbol())
                .name(tradeAssetEntity.getName())
                .enabled(tradeAssetEntity.isEnabled())
                .holdRatio(tradeAssetEntity.getHoldRatio())
                .build();
    }

}
