package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Macd {

    @Builder.Default
    private Double value = 0.0;

    @Builder.Default
    private Double signal = 0.0;

    @Builder.Default
    private Double oscillator = 0.0;

}
