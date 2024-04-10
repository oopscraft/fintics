package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Strategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyRequest {

    private String strategyName;

    private Strategy.Language language;

    private String variables;

    private String script;

}
