package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.MathContext;

@SuperBuilder
public abstract class CalculateContext {

    @Getter
    @Builder.Default
    private MathContext mathContext = MathContext.DECIMAL32;

}
