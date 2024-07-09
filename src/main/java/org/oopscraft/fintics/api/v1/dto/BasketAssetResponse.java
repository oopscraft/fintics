package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.BasketAsset;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAssetResponse extends AssetResponse {

    private String basketId;

    private String assetId;

    private Integer sort;

    private boolean fixed;

    private boolean enabled;

    private BigDecimal holdingWeight;

    public static BasketAssetResponse from(BasketAsset basketAsset) {
        return BasketAssetResponse.builder()
                .basketId(basketAsset.getBasketId())
                .assetId(basketAsset.getAssetId())
                .assetName(basketAsset.getAssetName())
                .market(basketAsset.getMarket())
                .type(basketAsset.getType())
                .exchange(basketAsset.getExchange())
                .marketCap(basketAsset.getMarketCap())
                .fixed(basketAsset.isFixed())
                .enabled(basketAsset.isEnabled())
                .holdingWeight(basketAsset.getHoldingWeight())
                .sort(basketAsset.getSort())
                .icon(basketAsset.getIcon())
                .links(LinkResponse.from(basketAsset.getLinks()))
                .assetMetas(AssetMetaResponse.from(basketAsset.getAssetMetas()))
                .build();
    }

}
