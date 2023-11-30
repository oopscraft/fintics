package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class CoContext extends CalculateContext {

    public static final CoContext DEFAULT = CoContext.of(3, 10, 9);

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    public static CoContext of(int shortPeriod, int longPeriod, int signalPeriod) {
        return CoContext.builder()
                .shortPeriod(shortPeriod)
                .longPeriod(longPeriod)
                .signalPeriod(signalPeriod)
                .build();
    }

}
