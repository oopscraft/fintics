package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.TradeAsset;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetResponse {

    private String tradeId;

    private String symbol;

    private String name;

    private boolean enabled;

    private BigDecimal holdRatio;

    public static TradeAssetResponse from(TradeAsset tradeAsset) {
        return TradeAssetResponse.builder()
                .tradeId(tradeAsset.getTradeId())
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
                .enabled(tradeAsset.isEnabled())
                .holdRatio(tradeAsset.getHoldRatio())
                .build();
    }

}
