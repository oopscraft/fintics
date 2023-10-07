package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.AssetEntity;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String symbol;

    private String name;

    private AssetType type;

    private String country;

    private String currency;

    private BigDecimal eps;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal dividendYield;

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .symbol(assetEntity.getSymbol())
                .name(assetEntity.getName())
                .country(assetEntity.getCountry())
                .currency(assetEntity.getCurrency())
                .eps(assetEntity.getEps())
                .roe(assetEntity.getRoe())
                .roa(assetEntity.getRoa())
                .dividendYield(assetEntity.getDividendYield())
                .build();
    }

}
