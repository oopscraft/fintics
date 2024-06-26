package org.oopscraft.fintics.indicator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class WilliamsR extends Indicator {

    private final BigDecimal value;

    private final BigDecimal signal;

}
