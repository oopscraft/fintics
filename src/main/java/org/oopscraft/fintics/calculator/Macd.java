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
public class Macd extends CalculateResult {

    private BigDecimal value;

    private BigDecimal signal;

    private BigDecimal oscillator;

}
