package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeAssetStatusEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAsset extends Asset {

    private String tradeId;

    private String assetId;

    private Integer sort;

    private boolean enabled;

    private BigDecimal holdingWeight;

    private TradeAssetStatus tradeAssetStatus;

    public static TradeAsset from(TradeAssetEntity tradeAssetEntity) {
        TradeAsset tradeAsset = TradeAsset.builder()
                .tradeId(tradeAssetEntity.getTradeId())
                .assetId(tradeAssetEntity.getAssetId())
                .sort(tradeAssetEntity.getSort())
                .enabled(tradeAssetEntity.isEnabled())
                .holdingWeight(tradeAssetEntity.getHoldingWeight())
                .build();

        // trade asset status entity
        TradeAssetStatusEntity tradeAssetStatusEntity = tradeAssetEntity.getTradeAssetStatusEntity();
        if (tradeAssetStatusEntity != null) {
            tradeAsset.setTradeAssetStatus(TradeAssetStatus.from(tradeAssetStatusEntity));
        } else {
            tradeAsset.setTradeAssetStatus(TradeAssetStatus.builder()
                    .tradeId(tradeAsset.getTradeId())
                    .assetId(tradeAsset.getAssetId())
                    .build());
        }

        // asset entity
        AssetEntity assetEntity = tradeAssetEntity.getAssetEntity();
        if(assetEntity != null) {
            tradeAsset.setAssetName(assetEntity.getAssetName());
            tradeAsset.setMarket(assetEntity.getMarket());
            tradeAsset.setExchange(assetEntity.getExchange());
            tradeAsset.setType(assetEntity.getType());
        }
        return tradeAsset;
    }

}
