package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.MathContext;

@SuperBuilder
@Getter
public class EmaContext extends CalculateContext {

    public static final EmaContext DEFAULT = EmaContext.of(10);

    private final int period;

    public static EmaContext of(int period) {
        return EmaContext.builder()
                .period(period)
                .build();
    }

    public static EmaContext of(int period, MathContext mathContext) {
        return EmaContext.builder()
                .period(period)
                .mathContext(mathContext)
                .build();
    }

}
