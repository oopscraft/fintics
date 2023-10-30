package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

    private List<LocalDateTime> getDateTimes(List<Ohlcv> ohlcvs) {
        return ohlcvs.stream()
                .map(Ohlcv::getDateTime)
                .collect(Collectors.toList());
    }

    public List<LocalDateTime> getMinuteDateTimes() {
        return getDateTimes(this.minuteOhlcvs);
    }

    public List<LocalDateTime> getDailyDateTimes() {
        return getDateTimes(this.dailyOhlcvs);
    }

    private List<BigDecimal> getPrices(List<Ohlcv> ohlcvs) {
        return ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
    }

    public List<BigDecimal> getMinutePrices() {
        return getPrices(this.minuteOhlcvs);
    }

    public List<BigDecimal> getDailyPrices() {
        return getPrices(this.dailyOhlcvs);
    }

    private List<BigDecimal> getSmas(List<BigDecimal> prices, int period) {
        List<BigDecimal> series = reverse(prices);
        List<BigDecimal> smas = SmaCalculator.of(series, period).calculate();
        return reverse(smas);
    }

    private BigDecimal getSma(List<BigDecimal> prices, int period) {
        List<BigDecimal> smas = getSmas(prices, period);
        return smas.isEmpty() ? BigDecimal.ZERO : smas.get(0);
    }

    public List<BigDecimal> getMinuteSmas(int period) {
        return getSmas(getMinutePrices(), period);
    }

    public BigDecimal getMinuteSma(int period) {
        return getSma(getMinutePrices(), period);
    }

    public List<BigDecimal> getDailySmas(int period) {
        return getSmas(getDailyPrices(), period);
    }

    public BigDecimal getDailySma(int period) {
        return getSma(getDailyPrices(), period);
    }

    private List<BigDecimal> getEmas(List<BigDecimal> prices, int period) {
        List<BigDecimal> series = reverse(prices);
        List<BigDecimal> emas = EmaCalculator.of(series, period).calculate();
        return reverse(emas);
    }

    private BigDecimal getEma(List<BigDecimal> prices, int period) {
        List<BigDecimal> emas = getEmas(prices, period);
        return emas.isEmpty() ? BigDecimal.ZERO : emas.get(0);
    }

    public List<BigDecimal> getMinuteEmas(int period) {
        return getEmas(getMinutePrices(), period);
    }

    public BigDecimal getMinuteEma(int period) {
        return getEma(getMinutePrices(), period);
    }

    public List<BigDecimal> getDailyEmas(int period) {
        return getEmas(getDailyPrices(), period);
    }

    public BigDecimal getDailyEma(int period) {
        return getEma(getDailyPrices(), period);
    }

    private List<Macd> getMacds(List<BigDecimal> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<BigDecimal> series = reverse(prices);
        List<Macd> macds = MacdCalculator.of(series, shortPeriod, longPeriod, signalPeriod).calculate();
        return reverse(macds);
    }

    private Macd getMacd(List<BigDecimal> prices, int shortPeriod, int longPeriod, int signalPeriod) {
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

    private List<BigDecimal> getRsis(List<BigDecimal> prices, int period) {
        List<BigDecimal> series = reverse(prices);
        List<BigDecimal> rsis = RsiCalculator.of(series, period).calculate();
        return reverse(rsis);
    }

    private BigDecimal getRsi(List<BigDecimal> prices, int period) {
        List<BigDecimal> rsis = getRsis(prices, period);
        return rsis.isEmpty() ? BigDecimal.valueOf(50.0) : rsis.get(0);
    }

    public List<BigDecimal> getMinuteRsis(int period) {
        return getRsis(getMinutePrices(), period);
    }

    public BigDecimal getMinuteRsi(int period) {
        return getRsi(getMinutePrices(), period);
    }

    public List<BigDecimal> getDailyRsis(int period) {
        return getRsis(getDailyPrices(), period);
    }

    public BigDecimal getDailyRsi(int period) {
        return getRsi(getDailyPrices(), period);
    }

    private List<Dmi> getDmis(List<Ohlcv> prices, int period) {
        List<Ohlcv> series = reverse(prices);
        List<BigDecimal> highSeries = series.stream()
                .map(Ohlcv::getHighPrice)
                .collect(Collectors.toList());
        List<BigDecimal> lowSeries = series.stream()
                .map(Ohlcv::getLowPrice)
                .collect(Collectors.toList());
        List<BigDecimal> closeSeries = series.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        List<Dmi> dmis = DmiCalculator.of(highSeries, lowSeries, closeSeries, period).calculate();
        return reverse(dmis);
    }

    private Dmi getDmi(List<Ohlcv> prices, int period) {
        List<Dmi> adxs = getDmis(prices, period);
        return adxs.isEmpty() ? Dmi.builder().build() : adxs.get(0);
    }

    public List<Dmi> getMinuteDmis(int period) {
        return getDmis(getMinuteOhlcvs(), period);
    }

    public Dmi getMinuteDmi(int period) {
        return getDmi(getMinuteOhlcvs(), period);
    }

    public List<Dmi> getDailyDmis(int period) {
        return getDmis(getDailyOhlcvs(), period);
    }

    public Dmi getDailyDmi(int period) {
        return getDmi(getDailyOhlcvs(), period);
    }

}