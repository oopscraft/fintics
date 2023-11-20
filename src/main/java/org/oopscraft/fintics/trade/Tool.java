package org.oopscraft.fintics.trade;

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

    public BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal mean(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(values);
        return series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32)
                .setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal median(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        int size = sortedValues.size();
        if (size % 2 == 1) {
            return sortedValues.get(size / 2);
        } else {
            BigDecimal leftMiddle = sortedValues.get(size / 2 - 1);
            BigDecimal rightMiddle = sortedValues.get(size / 2);
            return leftMiddle.add(rightMiddle)
                    .divide(BigDecimal.valueOf(2), MathContext.DECIMAL32)
                    .setScale(4, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal std(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(values);
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
        return standardDeviation.setScale(4, RoundingMode.HALF_UP);
    }

    public List<BigDecimal> pctChange(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);
        List<BigDecimal> pctChanges = new ArrayList<>();
        pctChanges.add(BigDecimal.ZERO);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal current = series.get(i);
            BigDecimal previous = series.get(i - 1);
            if(previous.compareTo(BigDecimal.ZERO) == 0) {
                if(current.compareTo(BigDecimal.ZERO) == 0) {
                    pctChanges.add(BigDecimal.ZERO);
                }else{
                    pctChanges.add(BigDecimal.valueOf(100));
                }
                continue;
            }
            BigDecimal pctChange = current.subtract(previous)
                    .divide(previous, MathContext.DECIMAL32)
                    .setScale(4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            pctChanges.add(pctChange);
        }
        return pctChanges;
    }

    public List<BigDecimal> zScore(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<BigDecimal>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        BigDecimal mean = mean(series);
        BigDecimal std = std(series);

        List<BigDecimal> zScores = new ArrayList();
        for(BigDecimal value : values) {
            if(std.compareTo(BigDecimal.ZERO) == 0) {
                zScores.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal zScore = value
                    .subtract(mean)
                    .divide(std, MathContext.DECIMAL32)
                    .setScale(4, RoundingMode.HALF_UP);
            zScores.add(zScore);
        }
        return zScores;
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















    @Deprecated
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

    @Deprecated
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






}
