package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.common.pbe.PbePropertiesUtil;
import org.oopscraft.arch4j.core.common.pbe.PbeStringUtil;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.BasketAssetEntity;

import java.math.BigDecimal;
import java.util.Properties;

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

    private String variables;

    /**
     * gets specified value
     * @param name namme
     * @return value
     */
    public String getVariable(String name) {
        return PbePropertiesUtil.loadProperties(variables)
                .getProperty(name, null);
    }

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
                .variables(basketAssetEntity.getVariables())
                .build();

        // asset entity
        AssetEntity assetEntity = basketAssetEntity.getAssetEntity();
        if(assetEntity != null) {
            basketAsset.setName(assetEntity.getName());
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
