package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.TradeAssetEntity;

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

    private BigDecimal holdRatio;

    public static TradeAsset from(TradeAssetEntity tradeAssetEntity) {
        TradeAsset tradeAsset = TradeAsset.builder()
                .tradeId(tradeAssetEntity.getTradeId())
                .assetId(tradeAssetEntity.getAssetId())
                .sort(tradeAssetEntity.getSort())
                .enabled(tradeAssetEntity.isEnabled())
                .holdRatio(tradeAssetEntity.getHoldRatio())
                .build();
        AssetEntity assetEntity = tradeAssetEntity.getAssetEntity();
        if(assetEntity != null) {
            tradeAsset.setAssetName(assetEntity.getAssetName());
            tradeAsset.setMarket(assetEntity.getMarket());
            tradeAsset.setExchange(assetEntity.getExchange());
            tradeAsset.setType(assetEntity.getType());
            tradeAsset.setMarketCap(assetEntity.getMarketCap());
            tradeAsset.setPer(assetEntity.getPer());
            tradeAsset.setRoe(assetEntity.getRoe());
            tradeAsset.setRoa(assetEntity.getRoa());
        }
        return tradeAsset;
    }

}
