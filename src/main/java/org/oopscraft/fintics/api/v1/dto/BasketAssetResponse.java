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

    private String variables;

    public static BasketAssetResponse from(BasketAsset basketAsset) {
        return BasketAssetResponse.builder()
                .basketId(basketAsset.getBasketId())
                .assetId(basketAsset.getAssetId())
                .name(basketAsset.getName())
                .market(basketAsset.getMarket())
                .type(basketAsset.getType())
                .exchange(basketAsset.getExchange())
                .updatedDate(basketAsset.getUpdatedDate())
                .marketCap(basketAsset.getMarketCap())
                .per(basketAsset.getPer())
                .eps(basketAsset.getEps())
                .roe(basketAsset.getRoe())
                .roa(basketAsset.getRoa())
                .dividendYield(basketAsset.getDividendYield())
                .fixed(basketAsset.isFixed())
                .enabled(basketAsset.isEnabled())
                .holdingWeight(basketAsset.getHoldingWeight())
                .variables(basketAsset.getVariables())
                .sort(basketAsset.getSort())
                .icon(basketAsset.getIcon())
                .links(LinkResponse.from(basketAsset.getLinks()))
                .build();
    }

}
