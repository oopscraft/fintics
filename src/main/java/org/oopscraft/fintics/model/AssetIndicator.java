package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.calculator.MacdCalculator;
import org.oopscraft.fintics.calculator.RsiCalculator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AssetIndicator extends Asset {

    private final LocalDateTime collectedAt;

    private final Double price;

    private final List<AssetTransaction> minuteAssetTransactions;

    private final List<AssetTransaction> dailyAssetTransactions;

    private List<Macd> minuteMacds;

    private List<Macd> dailyMacds;

    private List<Double> minuteRsis;

    private List<Double> dailyRsis;

    @Setter
    private Boolean holdConditionResult;

    @Builder
    public AssetIndicator(Asset asset, Double price, List<AssetTransaction> minuteAssetTransactions, List<AssetTransaction> dailyAssetTransactions) {
        setSymbol(asset.getSymbol());
        setName(asset.getName());
        setType(asset.getType());
        this.collectedAt = LocalDateTime.now();
        this.price = price;
        this.minuteAssetTransactions = minuteAssetTransactions;
        this.dailyAssetTransactions = dailyAssetTransactions;

        // initialize
        initialize();
    }

    private void initialize() {
        // minute prices
        List<Double> minutePrices = reverse(this.minuteAssetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList()));

        // daily prices
        List<Double> dailyPrices = reverse(this.minuteAssetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList()));

        // minute macds
        minuteMacds = reverse(MacdCalculator.of(minutePrices, 12, 26, 9)
                .calculate());

        // daily macds
        dailyMacds = reverse(MacdCalculator.of(dailyPrices, 12, 26, 9)
                .calculate());

        // minute rsis
        minuteRsis = reverse(RsiCalculator.of(minutePrices, 14)
                .calculate());

        // daily rsis
        dailyRsis = reverse(RsiCalculator.of(dailyPrices, 14)
                .calculate());
    }

    private static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    public Macd getMinuteMacd(int index) {
        return minuteMacds.get(index);
    }

    public Macd getMinuteMacd() {
        return getMinuteMacd(0);
    }

    public Macd getDailyMacd(int index) {
        return dailyMacds.get(index);
    }

    public Macd getDailyMacd() {
        return getDailyMacd(0);
    }

    public Double getMinuteRsi(int index) {
        return minuteRsis.get(index);
    }

    public Double getMinuteRsi() {
        return getMinuteRsi(0);
    }

    public Double getDailyRsi(int index) {
        return dailyRsis.get(index);
    }

    public Double getDailyRsi() {
        return getDailyRsi(0);
    }

}