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

    private Macd minuteMacd;

    private Macd dailyMacd;

    private Double minuteRsi;

    private Double dailyRsi;

    private Boolean holdConditionResult;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .collectedAt(assetIndicator.getCollectedAt())
                .price(assetIndicator.getPrice())
                .minuteMacd(assetIndicator.getMinuteMacd())
                .dailyMacd(assetIndicator.getDailyMacd())
                .minuteRsi(assetIndicator.getMinuteRsi())
                .dailyRsi(assetIndicator.getDailyRsi())
                .holdConditionResult(assetIndicator.getHoldConditionResult())
                .build();
    }

}
