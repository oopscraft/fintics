package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class DisparityIndexContext extends CalculateContext {

    private final int period;

    public static DisparityIndexContext of(int period) {
        return DisparityIndexContext.builder()
                .period(period)
                .build();
    }

}
