package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AdContext extends CalculateContext {

    public static final AdContext DEFAULT = AdContext.of();

    public static AdContext of() {
        return AdContext.builder()
                .build();
    }

}
