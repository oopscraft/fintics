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

    private List<Double> minutePrices;

    private List<Double> dailyPrices;

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
        minutePrices = this.minuteAssetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());

        // daily prices
        dailyPrices = this.minuteAssetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());

        // minute macds
        minuteMacds = reverse(MacdCalculator.of(reverse(minutePrices), 12, 26, 9)
                .calculate());

        // daily macds
        dailyMacds = reverse(MacdCalculator.of(reverse(dailyPrices), 12, 26, 9)
                .calculate());

        // minute rsis
        minuteRsis = reverse(RsiCalculator.of(reverse(minutePrices), 14)
                .calculate());

        // daily rsis
        dailyRsis = reverse(RsiCalculator.of(reverse(dailyPrices), 14)
                .calculate());
    }

    private static <T> List<T> reverse(List<T> list) {
        List<T> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);
        return reversedList;
    }

    public Macd getMinuteMacd(int index) {
        return index > minuteMacds.size() - 1
                ? Macd.builder()
                .macd(0.0)
                .signal(0.0)
                .oscillator(0.0)
                .build()
                : minuteMacds.get(index);
    }

    public Macd getMinuteMacd() {
        return getMinuteMacd(0);
    }

    public Macd getDailyMacd(int index) {
        return index > dailyMacds.size() - 1
                ? Macd.builder()
                .macd(0.0)
                .signal(0.0)
                .oscillator(0.0)
                .build()
                : dailyMacds.get(index);
    }

    public Macd getDailyMacd() {
        return getDailyMacd(0);
    }

    public Double getMinuteRsi(int index) {
        return index > minuteRsis.size() -1
                ? 0.0
                : minuteRsis.get(index);
    }

    public Double getMinuteRsi() {
        return getMinuteRsi(0);
    }

    public Double getDailyRsi(int index) {
        return index > dailyRsis.size() -1
                ? 0.0
                : dailyRsis.get(index);
    }

    public Double getDailyRsi() {
        return getDailyRsi(0);
    }

}