package org.oopscraft.fintics.calculator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class Obv extends CalculateResult {

    private final BigDecimal value;

    private final BigDecimal signal;

}
