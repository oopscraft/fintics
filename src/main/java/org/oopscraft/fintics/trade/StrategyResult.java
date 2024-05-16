package org.oopscraft.fintics.trade;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class StrategyResult {

    @EqualsAndHashCode.Include
    private final BigDecimal value;

    private final Object detail;

    public static StrategyResult of(BigDecimal value, Object detail) {
        return StrategyResult.builder()
                .value(value)
                .detail(detail)
                .build();
    }

}
