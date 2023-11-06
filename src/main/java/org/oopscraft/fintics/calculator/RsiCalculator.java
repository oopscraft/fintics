package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RsiCalculator {

    private final List<BigDecimal> series;

    private final int period;

    private final MathContext mathContext;

    public static RsiCalculator of(List<BigDecimal> series, int period) {
        return of(series, period, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static RsiCalculator of(List<BigDecimal> series, int period, MathContext mathContext) {
        return new RsiCalculator(series, period, mathContext);
    }

    public RsiCalculator(List<BigDecimal> series, int period, MathContext mathContext) {
        this.series = series;
        this.period = period;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {

        // price changes
        List<BigDecimal> priceChanges = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            if(i == 0) {
                priceChanges.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal priceChange = series.get(i)
                    .subtract(series.get(i - 1));
            priceChanges.add(priceChange);
        }

        // gain/loss values
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        for (BigDecimal priceChange : priceChanges) {
            if(priceChange.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(priceChange);
                losses.add(BigDecimal.ZERO);
            }else if(priceChange.compareTo(BigDecimal.ZERO) < 0) {
                gains.add(BigDecimal.ZERO);
                losses.add(priceChange.multiply(BigDecimal.valueOf(-1)));
            }else{
                gains.add(BigDecimal.ZERO);
                losses.add(BigDecimal.ZERO);
            }
        }

        List<BigDecimal> rsis = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            // period data of gain/loss
            List<BigDecimal> periodGains = gains.subList(
                Math.max(i - period + 1,0),
                i + 1
            );
            List<BigDecimal> periodLosses = losses.subList(
                Math.max(i - period + 1,0),
                i + 1
            );

            // 기간(period)+1 이전 평균은 정확한 기간평균이 아님으로 중립(50.00)으로 설정
            if(i < period + 1) {
                rsis.add(BigDecimal.valueOf(50.00));
                continue;
            }

            // average of gain/loss
            BigDecimal avgGain = getAverage(periodGains);
            BigDecimal avgLoss = getAverage(periodLosses);

            if(avgLoss.compareTo(BigDecimal.ZERO) == 0) {
                if(avgGain.compareTo(BigDecimal.ZERO) == 0) {
                    rsis.add(BigDecimal.ZERO);
                }else{
                    rsis.add(BigDecimal.valueOf(100.0));
                }
                continue;
            }

            // Calculate Relative Strength (RS)
            BigDecimal rs = avgLoss.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : avgGain.divide(avgLoss, MathContext.DECIMAL128)
                    .setScale(5, RoundingMode.HALF_UP);

            // Calculate RSI
            BigDecimal rsi = rs
                    .divide(rs.add(BigDecimal.valueOf(1)), MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            rsis.add(rsi);
        }

        return rsis;
    }

    private static BigDecimal getAverage(List<BigDecimal> values) {
        if(values.isEmpty()){
            return BigDecimal.ZERO;
        }
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL32);
    }

}
