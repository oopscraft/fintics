package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.calculator.MacdCalculator;
import org.oopscraft.fintics.calculator.RsiCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AssetIndicator extends Asset {

    private LocalDateTime collectedAt;

    private Double price;

    private Boolean holdConditionResult;

    @Builder.Default
    private List<AssetTransaction> minuteAssetTransaction = new ArrayList<>();

    @Builder.Default
    private List<AssetTransaction> dailyAssetTransactions = new ArrayList<>();

    public Macd getMinuteMacd() {
        return getMacd(minuteAssetTransaction);
    }

    public Macd getDailyMacd() {
        return getMacd(dailyAssetTransactions);
    }

    public static Macd getMacd(List<AssetTransaction> assetTransactions) {
        List<Double> prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<Macd> macds = MacdCalculator.of(prices, 12, 26, 9)
                .calculate();
        if(macds.size() > 0) {
            return macds.get(macds.size()-1);
        }else{
            return Macd.builder()
                    .series(0.0)
                    .macd(0.0)
                    .signal(0.0)
                    .oscillator(0.0)
                    .build();
        }
    }

    public Double getMinuteRsi() {
        return getRsi(minuteAssetTransaction);
    }

    public Double getDailyRsi() {
        return getRsi(dailyAssetTransactions);
    }

    public static Double getRsi(List<AssetTransaction> assetTransactions) {
        List<Double> prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<Double> rsiValues = RsiCalculator.of(prices, 14)
                .calculate();
        if(rsiValues.size() > 0) {
            return rsiValues.get(rsiValues.size() - 1);
        }else{
            return 50.0;
        }
    }

}