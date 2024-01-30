package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.TradeAsset;

import java.math.BigDecimal;

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

    private BigDecimal holdRatio;

    public static TradeAssetResponse from(TradeAsset tradeAsset) {
        return TradeAssetResponse.builder()
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .links(AssetResponse.LinkResponse.from(tradeAsset.getLinks()))
                .enabled(tradeAsset.isEnabled())
                .holdRatio(tradeAsset.getHoldRatio())
                .build();
    }

}
