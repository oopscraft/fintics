package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Strategy;

@Builder
@Getter
public class StrategyResponse {

    private String ruleId;

    private String ruleName;

    private Strategy.Language language;

    private String variables;

    private String script;

    public static StrategyResponse from(Strategy strategy) {
        return StrategyResponse.builder()
                .ruleId(strategy.getStrategyId())
                .ruleName(strategy.getStrategyName())
                .language(strategy.getLanguage())
                .variables(strategy.getVariables())
                .script(strategy.getScript())
                .build();
    }

}
