package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.BooleanConverter;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.BasketAssetEntity;

import javax.persistence.Column;
import javax.persistence.Convert;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAsset extends Asset {

    private String basketId;

    private String assetId;

    private Integer sort;

    private boolean fixed;

    private boolean enabled;

    private BigDecimal holdingWeight;

    /**
     * from factory method
     * @param basketAssetEntity basket asset entity
     * @return basket
     */
    public static BasketAsset from(BasketAssetEntity basketAssetEntity) {
        BasketAsset basketAsset = BasketAsset.builder()
                .basketId(basketAssetEntity.getBasketId())
                .assetId(basketAssetEntity.getAssetId())
                .sort(basketAssetEntity.getSort())
                .fixed(basketAssetEntity.isFixed())
                .enabled(basketAssetEntity.isEnabled())
                .holdingWeight(basketAssetEntity.getHoldingWeight())
                .build();

        // asset entity
        AssetEntity assetEntity = basketAssetEntity.getAssetEntity();
        if(assetEntity != null) {
            basketAsset.setAssetName(assetEntity.getAssetName());
            basketAsset.setMarket(assetEntity.getMarket());
            basketAsset.setExchange(assetEntity.getExchange());
            basketAsset.setType(assetEntity.getType());
            basketAsset.setMarketCap(assetEntity.getMarketCap());

            // asset meta entities
            basketAsset.setAssetMetas(assetEntity.getAssetMetaEntities().stream()
                    .map(AssetMeta::from)
                    .toList());
        }

        // return
        return basketAsset;
    }

}
