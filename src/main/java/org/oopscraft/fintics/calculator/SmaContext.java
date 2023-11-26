package org.oopscraft.fintics.calculator;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SmaContext extends CalculateContext {

    public static final SmaContext DEFAULT = SmaContext.of(10);

    private final int period;

    public static SmaContext of(int period) {
        return SmaContext.builder()
                .period(period)
                .build();
    }

}
