package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.oopscraft.fintics.calculator.CalculateResult;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class Ema implements CalculateResult {

    private final BigDecimal value;

}
