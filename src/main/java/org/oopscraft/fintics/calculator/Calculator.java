package org.oopscraft.fintics.calculator;

import lombok.Getter;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public abstract class Calculator<C extends CalculateContext, R extends CalculateResult> {

    @Getter
    private final C context;

    public Calculator(C context) {
        this.context = context;
    }

    public abstract List<R> calculate(List<Ohlcv> series);

    public static List<BigDecimal> emas(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> emas = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), mathContext);
        BigDecimal ema = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal emaDiff = series.get(i).subtract(ema);
            ema = emaDiff
                    .multiply(multiplier, mathContext)
                    .add(ema);
            emas.add(ema);
        }
        return emas;
    }

    public static List<BigDecimal> smas(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> smas = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++) {
            List<BigDecimal> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );

            BigDecimal sum = BigDecimal.ZERO;
            for(BigDecimal value : periodSeries) {
                sum = sum.add(value);
            }

            BigDecimal sma = sum.divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            smas.add(sma);
        }
        return smas;
    }

    public static List<BigDecimal> stds(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> stds = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            List<BigDecimal> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );
            BigDecimal mean = periodSeries.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            BigDecimal sumSquaredDeviations = periodSeries.stream()
                    .map(x -> x.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal variance = sumSquaredDeviations
                    .divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            BigDecimal std = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
            stds.add(std);
        }
        return stds;
    }

}
