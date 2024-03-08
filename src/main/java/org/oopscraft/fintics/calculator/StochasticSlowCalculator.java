package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class StochasticSlowCalculator extends Calculator<StochasticSlowContext, StochasticSlow> {

    public StochasticSlowCalculator(StochasticSlowContext context) {
        super(context);
    }

    @Override
    public List<StochasticSlow> calculate(List<Ohlcv> series) {
        List<StochasticSlow> stochasticSlows = new ArrayList<>();
        // close prices
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
                .toList();

        // TODO
        int size = series.size();
        for (int i = getContext().getPeriod() - 1; i < size; i++) {
            BigDecimal high = series.subList(i - getContext().getPeriod() + 1, i + 1).stream()
                    .map(Ohlcv::getHighPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal low = series.subList(i - getContext().getPeriod() + 1, i + 1).stream()
                    .map(Ohlcv::getLowPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal close = series.get(i).getClosePrice();
            BigDecimal k = high.subtract(low).compareTo(BigDecimal.ZERO) > 0 ?
                    close.subtract(low).divide(high.subtract(low), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;

            // Calculate average %K to get Slow %K
            BigDecimal slowK;
            if (i >= getContext().getPeriod() + getContext().getPeriodK() - 2) {
                slowK = BigDecimal.ZERO;
                for (int j = i - getContext().getPeriodK() + 1; j <= i; j++) {
                    slowK = slowK.add(stochasticSlows.get(j - getContext().getPeriod() + 1).getSlowK());
                }
                slowK = slowK.divide(BigDecimal.valueOf(getContext().getPeriodK()), 4, RoundingMode.HALF_UP);
            } else {
                slowK = k; // If not enough data for Slow %K, use current %K
            }

            // Calculate Slow %D as moving average of Slow %K
            BigDecimal slowD;
            if (stochasticSlows.size() >= getContext().getPeriodD() - 1) {
                slowD = slowK; // Initialize slowD to be current slowK
                for (int j = stochasticSlows.size() - getContext().getPeriodD() + 1; j < stochasticSlows.size(); j++) {
                    slowD = slowD.add(stochasticSlows.get(j).getSlowK());
                }
                slowD = slowD.divide(BigDecimal.valueOf(getContext().getPeriodD()), 4, RoundingMode.HALF_UP);
            } else {
                slowD = slowK; // If not enough data for Slow %D, use current Slow %K
            }

            stochasticSlows.add(StochasticSlow.builder()
                    .slowK(slowK)
                    .slowD(slowD)
                    .build());
        }

        return stochasticSlows;
    }

}
