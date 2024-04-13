package org.oopscraft.fintics.indicator.stochasticslow;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class StochasticSlowContext extends IndicatorContext {

    public static final StochasticSlowContext DEFAULT = StochasticSlowContext.of(5, 3, 3);

    private final int period;

    private final int periodK;

    private final int periodD;

    public static StochasticSlowContext of(int period, int periodK, int periodD) {
        return StochasticSlowContext.builder()
                .period(period)
                .periodK(periodK)
                .periodD(periodD)
                .build();
    }

}
