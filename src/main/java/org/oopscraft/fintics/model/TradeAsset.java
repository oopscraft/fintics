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

    private BigDecimal holdRatio;

    public static TradeAsset from(TradeAssetEntity tradeAssetEntity) {
        return TradeAsset.builder()
                .tradeId(tradeAssetEntity.getTradeId())
                .assetId(tradeAssetEntity.getAssetId())
                .assetName(tradeAssetEntity.getAssetName())
                .enabled(tradeAssetEntity.isEnabled())
                .holdRatio(tradeAssetEntity.getHoldRatio())
                .build();
    }

}
