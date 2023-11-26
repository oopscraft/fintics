package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class WvadContext extends CalculateContext {

    public static final WvadContext DEFAULT = WvadContext.of();

    public static WvadContext of() {
        return WvadContext.builder()
                .build();
    }

}
