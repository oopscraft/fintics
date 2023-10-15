package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetTransaction;
import org.oopscraft.fintics.model.AssetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

//    private List<AssetTransaction> minuteAssetTransactions;

//    private List<AssetTransaction> dailyAssetTransactions;

//    private List<Macd> minuteMacds;

    private Macd minuteMacd;

//    private List<Macd> dailyMacds;

    private Macd dailyMacd;

//    private List<Double> minuteRsis;

    private Double minuteRsi;

//    private List<Double> dailyRsis;

    private Double dailyRsi;

    private Boolean holdConditionResult;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .type(assetIndicator.getType())
                .collectedAt(assetIndicator.getCollectedAt())
                .price(assetIndicator.getPrice())
//                .minuteAssetTransactions(assetIndicator.getMinuteAssetTransactions())
//                .dailyAssetTransactions(assetIndicator.getDailyAssetTransactions())
//                .minuteMacds(assetIndicator.getMinuteMacds())
                .minuteMacd(assetIndicator.getMinuteMacd())
//                .dailyMacds(assetIndicator.getDailyMacds())
                .dailyMacd(assetIndicator.getDailyMacd())
//                .minuteRsis(assetIndicator.getMinuteRsis())
                .minuteRsi(assetIndicator.getMinuteRsi())
//                .dailyRsis(assetIndicator.getDailyRsis())
                .dailyRsi(assetIndicator.getDailyRsi())
                .holdConditionResult(assetIndicator.getHoldConditionResult())
                .build();
    }

}
