package org.oopscraft.fintics.indicator.sma;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class SmaContext extends IndicatorContext {

    public static final SmaContext DEFAULT = SmaContext.of(10);

    private final int period;

    public static SmaContext of(int period) {
        return SmaContext.builder()
                .period(period)
                .build();
    }

}
