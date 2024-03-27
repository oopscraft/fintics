package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class StochasticSlowContext extends CalculateContext {

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
