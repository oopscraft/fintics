package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Strategy;

@Builder
@Getter
public class StrategyResponse {

    private String ruleId;

    private String ruleName;

    private String language;

    private String script;

    public static StrategyResponse from(Strategy rule) {
        return StrategyResponse.builder()
                .ruleId(rule.getStrategyId())
                .ruleName(rule.getStrategyName())
                .language(rule.getScript())
                .script(rule.getScript())
                .build();
    }

}
