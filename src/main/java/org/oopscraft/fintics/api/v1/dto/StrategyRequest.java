package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StrategyRequest {

    private String strategyId;

    private String strategyName;

    private String language;

    private String script;

}
