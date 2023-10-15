package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RsiCalculator {

    private final List<Double> series;

    private final Integer period;

    public static RsiCalculator of(List<Double> series, int period) {
        return new RsiCalculator(series, period);
    }

    public RsiCalculator(List<Double> series, int period) {
        this.series = series;
        this.period = period;
    }

    public List<Double> calculate() {
        List<Double> rsiValues = new ArrayList<>();

        // price changes
        List<Double> priceChanges = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            if(i == 0) {
                priceChanges.add(0.0);
                continue;
            }
            double priceChange = series.get(i) - series.get(i - 1);
            priceChanges.add(priceChange);
        }

        // gain/loss values
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        for (double priceChange : priceChanges) {
            if(priceChange > 0) {
                gains.add(priceChange);
                losses.add(0.0);
            }else if(priceChange < 0) {
                gains.add(0.0);
                losses.add(priceChange*-1);
            }else{
                gains.add(0.0);
                losses.add(0.0);
            }
        }

        for (int i = 0; i < series.size(); i++) {
            // period data of gain/loss
            List<Double> periodGains = gains.subList(
                Math.max(i - period + 1,0),
                i + 1
            );
            List<Double> periodLosses = losses.subList(
                Math.max(i - period + 1,0),
                i + 1
            );

            // 기간(period)+1 이전 평균은 정확한 기간평균이 아님으로 중립(50.00)으로 설정
            if(i < period + 1) {
                rsiValues.add(50.00);
                continue;
            }

            // average of gain/loss
            double avgGain = getAverage(periodGains);
            double avgLoss = getAverage(periodLosses);

            // Calculate Relative Strength (RS)
            BigDecimal rs = avgLoss == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(avgGain)
                            .divide(BigDecimal.valueOf(avgLoss), MathContext.DECIMAL128)
                            .setScale(5, RoundingMode.HALF_UP);

            // check zero
            if(rs.equals(BigDecimal.ZERO)) {
                rsiValues.add(100.0);
                continue;
            }

            // Calculate RSI
            BigDecimal rsi = rs
                    .divide(rs.add(BigDecimal.valueOf(1)), MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            rsiValues.add(rsi.doubleValue());
        }

        return rsiValues;
    }

    private static double getAverage(List<Double> gains) {
        if(gains.isEmpty()){
            return 0.0;
        }
        return new Mean().evaluate(gains.stream()
                .mapToDouble(Double::doubleValue)
                .toArray());
    }

}
