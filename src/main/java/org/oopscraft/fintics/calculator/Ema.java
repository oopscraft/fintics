package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@ToString
public class Ema extends CalculateResult {

    private final BigDecimal value;

}
