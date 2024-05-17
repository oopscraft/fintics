package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class StrategyResult {

    @EqualsAndHashCode.Include
    private BigDecimal value;

    private String detail;

    public static StrategyResult of(BigDecimal value, String detail) {
        return StrategyResult.builder()
                .value(value)
                .detail(detail)
                .build();
    }

}
