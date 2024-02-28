package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BbContext extends CalculateContext {

    public static final BbContext DEFAULT = BbContext.of(20, 2);

    private final int period;

    private final int stdMultiplier;

    public static BbContext of(int period, int stdMultiplier) {
        return BbContext.builder()
                .period(period)
                .stdMultiplier(stdMultiplier)
                .build();
    }

}
