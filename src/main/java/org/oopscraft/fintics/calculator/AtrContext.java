package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AtrContext extends CalculateContext {

    public static final AtrContext DEFAULT = AtrContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    public static AtrContext of(int period, int signalPeriod) {
        return AtrContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
