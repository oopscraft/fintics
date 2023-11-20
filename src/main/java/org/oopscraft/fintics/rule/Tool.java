package org.oopscraft.fintics.rule;

import org.oopscraft.fintics.calculator.*;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tool {

    public List<Ohlcv> resample(List<Ohlcv> ohlcvs, int period) {
        if (ohlcvs.isEmpty() || period <= 0) {
            return Collections.emptyList();
        }

        List<Ohlcv> resampledOhlcvs = new ArrayList<>();
        int dataSize = ohlcvs.size();
        int currentIndex = 0;

        while (currentIndex < dataSize) {
            int endIndex = Math.min(currentIndex + period, dataSize);
            List<Ohlcv> subList = ohlcvs.subList(currentIndex, endIndex);
            Ohlcv resampledData = createResampledOhlcv(subList);
            resampledOhlcvs.add(resampledData);
            currentIndex += period;
        }

        return resampledOhlcvs;
    }

    private Ohlcv createResampledOhlcv(List<Ohlcv> ohlcvs) {
        BigDecimal sumOpenPrice = BigDecimal.ZERO;
        BigDecimal sumHighPrice = BigDecimal.ZERO;
        BigDecimal sumLowPrice = BigDecimal.ZERO;
        BigDecimal sumClosePrice = BigDecimal.ZERO;
        BigDecimal sumVolume = BigDecimal.ZERO;

        for(Ohlcv ohlcv : ohlcvs) {
            sumOpenPrice = sumOpenPrice.add(ohlcv.getOpenPrice());
            sumHighPrice = sumHighPrice.add(ohlcv.getHighPrice());
            sumLowPrice = sumLowPrice.add(ohlcv.getLowPrice());
            sumClosePrice = sumClosePrice.add(ohlcv.getClosePrice());
            sumVolume = sumVolume.add(ohlcv.getVolume());
        }

        BigDecimal size = BigDecimal.valueOf(ohlcvs.size());
        OhlcvType ohlcvType = ohlcvs.get(ohlcvs.size()-1).getOhlcvType();
        LocalDateTime dateTime = ohlcvs.get(ohlcvs.size()-1).getDateTime();
        BigDecimal openPrice = sumOpenPrice.divide(size, MathContext.DECIMAL32)
                .setScale(sumOpenPrice.scale(), RoundingMode.HALF_UP);
        BigDecimal highPrice = sumHighPrice.divide(size, MathContext.DECIMAL32)
                .setScale(sumHighPrice.scale(), RoundingMode.HALF_UP);
        BigDecimal lowPrice = sumLowPrice.divide(size, MathContext.DECIMAL32)
                .setScale(sumLowPrice.scale(), RoundingMode.HALF_UP);
        BigDecimal closePrice = sumClosePrice.divide(size, MathContext.DECIMAL32)
                .setScale(sumClosePrice.scale(), RoundingMode.HALF_UP);
        return Ohlcv.builder()
                .ohlcvType(ohlcvType)
                .dateTime(dateTime)
                .openPrice(openPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .closePrice(closePrice)
                .volume(sumVolume)
                .build();
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
                .divide(standardDeviation, MathContext.DECIMAL32)
                .setScale(4, RoundingMode.HALF_UP);
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
                .setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
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
                .setScale(4, RoundingMode.HALF_UP);
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
