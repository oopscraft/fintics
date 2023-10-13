package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetIndicatorResponse {

    private String symbol;

    private String name;

    private AssetType type;

    private LocalDateTime collectedAt;

    private Double price;

    private Macd dailyMacd;

    private Macd minuteMacd;

    private Boolean holdConditionResult;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .collectedAt(assetIndicator.getCollectedAt())
                .price(assetIndicator.getPrice())
                .dailyMacd(assetIndicator.getDailyMacd())
                .minuteMacd(assetIndicator.getMinuteMacd())
                .holdConditionResult(assetIndicator.getHoldConditionResult())
                .build();
    }

}
