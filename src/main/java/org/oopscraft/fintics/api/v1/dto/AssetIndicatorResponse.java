package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetIndicatorResponse {

    private String symbol;

    private String name;

    private AssetType type;

    private BigDecimal price;

    private BigDecimal dailyMacd;

    private BigDecimal minuteMacd;

    private BigDecimal dailyRsi;

    private BigDecimal minuteRsi;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .price(assetIndicator.getPrice())
                .dailyMacd(assetIndicator.getDailyMacd())
                .minuteMacd(assetIndicator.getMinuteMacd())
                .dailyRsi(assetIndicator.getDailyRsi())
                .minuteRsi(assetIndicator.getMinuteRsi())
                .build();
    }

}
