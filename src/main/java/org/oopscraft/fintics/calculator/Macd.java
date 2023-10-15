package org.oopscraft.fintics.calculator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Macd {

    private Double series;

    private Double macd;

    private Double signal;

    private Double oscillator;

}
