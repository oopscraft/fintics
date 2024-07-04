package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.TradeAsset;

import java.math.BigDecimal;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetResponse extends AssetResponse {

    private String tradeId;

    private String assetId;

    private String assetName;

    private boolean enabled;

    private BigDecimal holdingWeight;

    private TradeAssetStatusResponse tradeAssetStatusResponse;

    public static TradeAssetResponse from(TradeAsset tradeAsset) {
        return TradeAssetResponse.builder()
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .links(LinkResponse.from(tradeAsset.getLinks()))
                .enabled(tradeAsset.isEnabled())
                .holdingWeight(tradeAsset.getHoldingWeight())
                .exchange(tradeAsset.getExchange())
                .type(tradeAsset.getType())
                .links(LinkResponse.from(tradeAsset.getLinks()))
                .assetFinancial(Optional.ofNullable(tradeAsset.getAssetFinancial())
                        .map(FinancialResponse::from)
                        .orElse(null))
                .tradeAssetStatusResponse(Optional.ofNullable(tradeAsset.getTradeAssetStatus())
                        .map(TradeAssetStatusResponse::from)
                        .orElse(null))
                .build();
    }

}
