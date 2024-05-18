package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.StrategyResult;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyResultResponse {

    private BigDecimal value;

    private String detail;

    public static StrategyResultResponse from(StrategyResult strategyResult) {
        return StrategyResultResponse.builder()
                .value(strategyResult.getPosition())
                .detail(strategyResult.getDescription())
                .build();
    }

}
