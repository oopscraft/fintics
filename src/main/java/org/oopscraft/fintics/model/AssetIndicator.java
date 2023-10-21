package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AssetIndicator {

    private final String symbol;

    private final String name;

    private final AssetType type;

    private final LocalDateTime collectedAt;

    private final OrderBook orderBook;

    private final List<Ohlcv> minuteOhlcvs;

    private final List<Ohlcv> dailyOhlcvs;

    @Builder
    public AssetIndicator(Asset asset, OrderBook orderBook, List<Ohlcv> minuteOhlcvs, List<Ohlcv> dailyOhlcvs) {
        this.symbol = asset.getSymbol();
        this.name = asset.getName();
        this.type = asset.getType();
        this.collectedAt = LocalDateTime.now();
        this.orderBook = orderBook;
        this.minuteOhlcvs = minuteOhlcvs;
        this.dailyOhlcvs = dailyOhlcvs;
    }

    private <T> List<T> reverse(List<T> list) {
        List<T> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);
        return reversedList;
    }

    private List<Double> getPrices(List<Ohlcv> ohlcvs) {
        return ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
    }

    public List<Double> getMinutePrices() {
        return getPrices(this.minuteOhlcvs);
    }

    public List<Double> getDailyPrices() {
        return getPrices(this.dailyOhlcvs);
    }

    private List<Double> getSmas(List<Double> prices, int period) {
        List<Double> series = reverse(prices);
        List<Double> smas = SmaCalculator.of(series, period).calculate();
        return reverse(smas);
    }

    private Double getSma(List<Double> prices, int period) {
        List<Double> smas = getSmas(prices, period);
        return smas.isEmpty() ? 0.0 : smas.get(0);
    }

    public List<Double> getMinuteSmas(int period) {
        return getSmas(getMinutePrices(), period);
    }

    public Double getMinuteSma(int period) {
        return getSma(getMinutePrices(), period);
    }

    public List<Double> getDailySmas(int period) {
        return getSmas(getDailyPrices(), period);
    }

    public Double getDailySma(int period) {
        return getSma(getDailyPrices(), period);
    }

    private List<Double> getEmas(List<Double> prices, int period) {
        List<Double> series = reverse(prices);
        List<Double> emas = EmaCalculator.of(series, period).calculate();
        return reverse(emas);
    }

    private Double getEma(List<Double> prices, int period) {
        List<Double> emas = getEmas(prices, period);
        return emas.isEmpty() ? 0.0 : emas.get(0);
    }

    public List<Double> getMinuteEmas(int period) {
        return getEmas(getMinutePrices(), period);
    }

    public Double getMinuteEma(int period) {
        return getEma(getMinutePrices(), period);
    }

    public List<Double> getDailyEmas(int period) {
        return getEmas(getDailyPrices(), period);
    }

    public Double getDailyEma(int period) {
        return getEma(getDailyPrices(), period);
    }

    private List<Macd> getMacds(List<Double> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<Double> series = reverse(prices);
        List<Macd> macds = MacdCalculator.of(series, shortPeriod, longPeriod, signalPeriod).calculate();
        return reverse(macds);
    }

    private Macd getMacd(List<Double> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<Macd> macds = getMacds(prices, shortPeriod, longPeriod, signalPeriod);
        return macds.isEmpty() ? Macd.builder().build() : macds.get(0);
    }

    public List<Macd> getMinuteMacds(int shortPeriod, int longPeriod, int signalPeriod) {
        return getMacds(getMinutePrices(), shortPeriod, longPeriod, signalPeriod);
    }

    public Macd getMinuteMacd(int shortPeriod, int longPeriod, int signalPeriod) {
        return getMacd(getMinutePrices(), shortPeriod, longPeriod, signalPeriod);
    }

    public List<Macd> getDailyMacds(int shortPeriod, int longPeriod, int signalPeriod) {
        return getMacds(getDailyPrices(), shortPeriod, longPeriod, signalPeriod);
    }

    public Macd getDailyMacd(int shortPeriod, int longPeriod, int signalPeriod) {
        return getMacd(getDailyPrices(), shortPeriod, longPeriod, signalPeriod);
    }

    private List<Double> getRsis(List<Double> prices, int period) {
        List<Double> series = reverse(prices);
        List<Double> rsis = RsiCalculator.of(series, period).calculate();
        return reverse(rsis);
    }

    private Double getRsi(List<Double> prices, int period) {
        List<Double> rsis = getRsis(prices, period);
        return rsis.isEmpty() ? 50.0 : rsis.get(0);
    }

    public List<Double> getMinuteRsis(int period) {
        return getRsis(getMinutePrices(), period);
    }

    public Double getMinuteRsi(int period) {
        return getRsi(getMinutePrices(), period);
    }

    public List<Double> getDailyRsis(int period) {
        return getRsis(getDailyPrices(), period);
    }

    public Double getDailyRsi(int period) {
        return getRsi(getDailyPrices(), period);
    }

}