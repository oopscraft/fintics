package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;
import org.oopscraft.fintics.model.OrderBook;

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

    private OrderBook orderBook;

    @Builder.Default
    private List<Double> minutePrices = new ArrayList<>();

    @Builder.Default
    private List<Double> dailyPrices = new ArrayList<>();

    @Builder.Default
    private List<Double> minuteMacdOscillators = new ArrayList<>();

    @Builder.Default
    private List<Double> dailyMacdOscillators = new ArrayList<>();

    @Builder.Default
    private List<Double> minuteRsis = new ArrayList<>();

    @Builder.Default
    private List<Double> dailyRsis = new ArrayList<>();

    private Boolean holdConditionResult;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .collectedAt(assetIndicator.getCollectedAt())
                .orderBook(assetIndicator.getOrderBook())
                .minutePrices(assetIndicator.getMinutePrices())
                .dailyPrices(assetIndicator.getDailyPrices())
                .minuteMacdOscillators(assetIndicator.getMinuteMacdOscillators())
                .dailyMacdOscillators(assetIndicator.getDailyMacdOscillators())
                .minuteRsis(assetIndicator.getMinuteRsis())
                .dailyRsis(assetIndicator.getDailyRsis())
                .holdConditionResult(assetIndicator.getHoldConditionResult())
                .build();
    }

}
