package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

    private final OrderBook orderBook;

    private final List<Ohlcv> minuteOhlcvs;

    private final List<Ohlcv> dailyOhlcvs;

    private List<Double> minutePrices;

    private List<Double> dailyPrices;

    private List<Double> minuteMacdOscillators;

    private List<Double> dailyMacdOscillators;

    private List<Double> minuteRsis;

    private List<Double> dailyRsis;

    @Setter
    private Boolean holdConditionResult;

    @Builder
    public AssetIndicator(Asset asset, OrderBook orderBook, List<Ohlcv> minuteOhlcvs, List<Ohlcv> dailyOhlcvs) {
        setSymbol(asset.getSymbol());
        setName(asset.getName());
        setType(asset.getType());
        this.collectedAt = LocalDateTime.now();
        this.orderBook = orderBook;
        this.minuteOhlcvs = minuteOhlcvs;
        this.dailyOhlcvs = dailyOhlcvs;

        // initialize
        initialize();
    }

    private void initialize() {
        // minute prices
        minutePrices = this.minuteOhlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());

        // daily prices
        dailyPrices = this.minuteOhlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());

        // minute macd oscillators
        minuteMacdOscillators = reverse(MacdCalculator.of(reverse(minutePrices), 12, 26, 9)
                .getOscillators());

        // daily macd oscillators
        dailyMacdOscillators = reverse(MacdCalculator.of(reverse(dailyPrices), 12, 26, 9)
                .getOscillators());

        // minute rsis
        minuteRsis = reverse(RsiCalculator.of(reverse(minutePrices), 14)
                .getRsis());

        // daily rsis
        dailyRsis = reverse(RsiCalculator.of(reverse(dailyPrices), 14)
                .getRsis());
    }

    private static <T> List<T> reverse(List<T> list) {
        List<T> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);
        return reversedList;
    }

    public Double getMinuteMacdOscillator(int index) {
        return index > minuteMacdOscillators.size() - 1
                ? 0.0
                : minuteMacdOscillators.get(index);
    }

    public Double getMinuteMacdOscillator() {
        return getMinuteMacdOscillator(0);
    }

    public Double getDailyMacdOscillator(int index) {
        return index > dailyMacdOscillators.size() - 1
                ? 0.0
                : dailyMacdOscillators.get(index);
    }

    public Double getDailyMacdOscillator() {
        return getDailyMacdOscillator(0);
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