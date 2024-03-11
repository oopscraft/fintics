package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class CciContext extends CalculateContext {

    public static final CciContext DEFAULT = CciContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    public static CciContext of(int period, int signalPeriod) {
        return CciContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
