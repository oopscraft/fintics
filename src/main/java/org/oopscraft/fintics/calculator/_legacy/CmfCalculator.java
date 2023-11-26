package org.oopscraft.fintics.calculator._legacy;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CmfCalculator {


    private final List<Ohlcv> series;

    private final int period;

    private final MathContext mathContext;

    public static CmfCalculator of(List<Ohlcv> series, int period) {
        return of(series, period, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static CmfCalculator of(List<Ohlcv> series, int period, MathContext mathContext) {
        return new CmfCalculator(series, period, mathContext);
    }

    public CmfCalculator(List<Ohlcv> series, int period, MathContext mathContext) {
        this.series = series;
        this.period = period;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> cmfs = new ArrayList<BigDecimal>();
        return cmfs;
    }

}
