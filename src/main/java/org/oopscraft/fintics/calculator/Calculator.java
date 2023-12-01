package org.oopscraft.fintics.calculator;

import lombok.Getter;
import org.oopscraft.fintics.model.TradeAssetOhlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class Calculator<C extends CalculateContext, R extends CalculateResult> {

    @Getter
    private final C context;

    public Calculator(C context) {
        this.context = context;
    }

    public abstract List<R> calculate(List<TradeAssetOhlcv> series);

    protected final List<BigDecimal> emas(List<BigDecimal> series, int period) {
        List<BigDecimal> emas = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), getContext().getMathContext());
        BigDecimal ema = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal emaDiff = series.get(i).subtract(ema);
            ema = emaDiff
                    .multiply(multiplier, getContext().getMathContext())
                    .add(ema);
            emas.add(ema);
        }
        return emas;
    }

    protected final List<BigDecimal> smas(List<BigDecimal> series, int period) {
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

            BigDecimal sma = sum.divide(BigDecimal.valueOf(periodSeries.size()), getContext().getMathContext());
            smas.add(sma);
        }
        return smas;
    }

}
