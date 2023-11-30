package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class Co implements CalculateResult {

    private final BigDecimal value;

    private final BigDecimal signal;

}
