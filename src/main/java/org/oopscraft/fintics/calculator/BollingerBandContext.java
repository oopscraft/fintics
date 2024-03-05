package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BollingerBandContext extends CalculateContext {

    public static final BollingerBandContext DEFAULT = BollingerBandContext.of(20, 2);

    private final int period;

    private final int stdMultiplier;

    public static BollingerBandContext of(int period, int stdMultiplier) {
        return BollingerBandContext.builder()
                .period(period)
                .stdMultiplier(stdMultiplier)
                .build();
    }

}
