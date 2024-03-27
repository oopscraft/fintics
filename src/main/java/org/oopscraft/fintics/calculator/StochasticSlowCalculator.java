package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class StochasticSlowCalculator extends Calculator<StochasticSlowContext, StochasticSlow> {

    public StochasticSlowCalculator(StochasticSlowContext context) {
        super(context);
    }

    @Override
    public List<StochasticSlow> calculate(List<Ohlcv> series) {
        int period = getContext().getPeriod();
        int periodK = getContext().getPeriodK();
        int periodD = getContext().getPeriodD();
        MathContext mathContext = getContext().getMathContext();

        // rawK
        List<BigDecimal> rawKs = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            List<Ohlcv> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );
            BigDecimal high = periodSeries.stream()
                    .map(Ohlcv::getHighPrice)
                    .max(BigDecimal::compareTo)
                    .get();
            BigDecimal low = periodSeries.stream()
                    .map(Ohlcv::getLowPrice)
                    .min(BigDecimal::compareTo)
                    .get();
            BigDecimal close = series.get(i).getClosePrice();

            BigDecimal rawK = BigDecimal.valueOf(50);   // neutral
            if(high.compareTo(low) != 0) {
                rawK = close.subtract(low)
                        .divide(high.subtract(low), mathContext)
                        .multiply(BigDecimal.valueOf(100));
            }
            rawKs.add(rawK);
        }

        // slowK
        List<BigDecimal> slowKs = emas(rawKs, periodK, mathContext);

        // slowD
        List<BigDecimal> slowDs = emas(slowKs, periodD, mathContext);

        // stochastic slow
        List<StochasticSlow> stochasticSlows = new ArrayList<>();
        for (int i = 0; i < slowDs.size(); i++) {
            StochasticSlow stochasticSlow = StochasticSlow.builder()
                    .slowK(slowKs.get(i))
                    .slowD(slowDs.get(i))
                    .build();
            stochasticSlows.add(stochasticSlow);
        }

        // return
        return stochasticSlows;
    }

}
