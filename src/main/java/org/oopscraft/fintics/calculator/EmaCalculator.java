package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator {

    private final List<BigDecimal> series;

    private final int period;

    private final MathContext mathContext;

    public static EmaCalculator of(List<BigDecimal> series, int period) {
        return new EmaCalculator(series, period, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static EmaCalculator of(List<BigDecimal> series, int period, MathContext mathContext) {
        return new EmaCalculator(series, period, mathContext);
    }

    public EmaCalculator(List<BigDecimal> series, int period, MathContext mathContext) {
        this.series = series;
        this.period = period;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> emas = new ArrayList<>();

        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), mathContext);

        BigDecimal ema = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal emaDiff = series.get(i).subtract(ema);
            ema = emaDiff
                    .multiply(multiplier)
                    .add(ema);
            emas.add(ema);
        }

        return emas;
    }

}
