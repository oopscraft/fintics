package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MmaCalculator {

    private final List<BigDecimal> series;

    private final int period;

    public static MmaCalculator of(List<BigDecimal> series, int period) {
        return new MmaCalculator(series, period);
    }

    public MmaCalculator(List<BigDecimal> series, int period) {
        this.series = series;
        this.period = period;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> mmas = new ArrayList<>();

        BigDecimal multiplier = BigDecimal.valueOf(1.0)
                .divide(BigDecimal.valueOf(period), MathContext.DECIMAL128);

        BigDecimal mma = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        mmas.add(mma);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal mmaDiff = series.get(i).subtract(mma);
            mma = mmaDiff.multiply(multiplier)
                    .add(mma)
                    .setScale(2, RoundingMode.HALF_UP);
            mmas.add(mma);
        }

        return mmas;
    }

}
