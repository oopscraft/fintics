package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RsiContext extends CalculateContext {

    public static final RsiContext DEFAULT = RsiContext.of(14);

    private final int period;

    public static RsiContext of(int period) {
        return RsiContext.builder()
                .period(period)
                .build();
    }

}
