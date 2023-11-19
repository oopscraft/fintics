package org.oopscraft.fintics.rule;

import org.oopscraft.fintics.calculator.*;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tool {

    public List<Ohlcv> resample(List<Ohlcv> ohlcvs, int period, String type) {
        // TODO
        return ohlcvs;
    }

    public BigDecimal zScore(List<BigDecimal> sampleValues, BigDecimal testValue) {
        if(sampleValues.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(sampleValues);
        Collections.reverse(series);

        BigDecimal mean = series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);

        BigDecimal sumSquaredDeviations = series.stream()
                .map(x -> x.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = sumSquaredDeviations
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        if(standardDeviation.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return testValue
                .subtract(mean)
                .divide(standardDeviation, MathContext.DECIMAL32);
    }

    public BigDecimal slope(List<BigDecimal> values) {
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        // check empty
        if(series.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // sum
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            BigDecimal change = series.get(i)
                    .subtract(series.get(Math.max(i-1,0)));
            sum = sum.add(change);
        }

        // average
        return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal average(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for(int i = 0, size = values.size(); i < size; i ++ ) {
            sum = sum.add(values.get(i));
        }
        return sum.divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<BigDecimal> sma(List<Ohlcv> ohlcvs, int period) {
        List<BigDecimal> prices = ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<BigDecimal> smas = SmaCalculator.of(prices, period).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        Collections.reverse(smas);
        return smas;
    }

    public List<BigDecimal> ema(List<Ohlcv> ohlcvs, int period) {
        List<BigDecimal> prices = ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<BigDecimal> emas = EmaCalculator.of(prices, period).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        Collections.reverse(emas);
        return emas;
    }

    public List<Macd> macd(List<Ohlcv> ohlcvs, int shortPeriod, int longPeriod, int signalPeriod) {
        List<BigDecimal> prices = ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<Macd> macds =  MacdCalculator.of(prices, shortPeriod, longPeriod, signalPeriod).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        Collections.reverse(macds);
        return macds;
    }

    public List<BigDecimal> rsi(List<Ohlcv> ohlcvs, int period) {
        List<BigDecimal> prices = ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<BigDecimal> rsis =  RsiCalculator.of(prices, period).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        Collections.reverse(rsis);
        return rsis;
    }

    public List<Dmi> dmi(List<Ohlcv> ohlcvs, int period) {
        List<BigDecimal> highSeries = ohlcvs.stream()
                .map(Ohlcv::getHighPrice)
                .collect(Collectors.toList());
        Collections.reverse(highSeries);

        List<BigDecimal> lowSeries = ohlcvs.stream()
                .map(Ohlcv::getLowPrice)
                .collect(Collectors.toList());
        Collections.reverse(lowSeries);

        List<BigDecimal> closeSeries = ohlcvs.stream()
                .map(Ohlcv::getClosePrice)
                .collect(Collectors.toList());
        Collections.reverse(closeSeries);

        List<Dmi> dmis = DmiCalculator.of(highSeries, lowSeries, closeSeries, period).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        Collections.reverse(dmis);

        return dmis;
    }





















}
