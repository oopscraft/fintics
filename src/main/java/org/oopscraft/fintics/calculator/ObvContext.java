package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ObvContext extends CalculateContext {

    public static final ObvContext DEFAULT = ObvContext.of(14);

    private final int period;

    public static ObvContext of(int period) {
        return ObvContext.builder()
                .period(period)
                .build();
    }

}
