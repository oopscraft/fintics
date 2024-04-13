package org.oopscraft.fintics.indicator.atr;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class AtrContext extends IndicatorContext {

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
