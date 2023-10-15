package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    private List<Double> minutePrices = new ArrayList<>();

    @Builder.Default
    private List<Double> dailyPrices = new ArrayList<>();

    private Macd minuteMacd;

    @Builder.Default
    private List<Macd> minuteMacds = new ArrayList<>();

    private Macd dailyMacd;

    @Builder.Default
    private List<Macd> dailyMacds = new ArrayList<>();

    private Double minuteRsi;

    @Builder.Default
    private List<Double> minuteRsis = new ArrayList<>();

    private Double dailyRsi;

    @Builder.Default
    private List<Double> dailyRsis = new ArrayList<>();

    private Boolean holdConditionResult;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .collectedAt(assetIndicator.getCollectedAt())
                .price(assetIndicator.getPrice())
                .minutePrices(assetIndicator.getMinutePrices())
                .dailyPrices(assetIndicator.getDailyPrices())
                .minuteMacd(assetIndicator.getMinuteMacd())
                .minuteMacds(assetIndicator.getMinuteMacds())
                .dailyMacd(assetIndicator.getDailyMacd())
                .dailyMacds(assetIndicator.getDailyMacds())
                .minuteRsi(assetIndicator.getMinuteRsi())
                .minuteRsis(assetIndicator.getMinuteRsis())
                .dailyRsi(assetIndicator.getDailyRsi())
                .dailyRsis(assetIndicator.getDailyRsis())
                .holdConditionResult(assetIndicator.getHoldConditionResult())
                .build();
    }

}
