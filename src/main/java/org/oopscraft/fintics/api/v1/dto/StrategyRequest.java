package org.oopscraft.fintics.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.oopscraft.fintics.model.Strategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "strategy request")
public class StrategyRequest {

    @Schema(description = "strategy id")
    private String strategyId;

    @Schema(description = "strategy name")
    private String strategyName;

    @Schema(description = "language")
    private Strategy.Language language;

    @Schema(description = "variables")
    private String variables;

    @Schema(description = "script")
    private String script;

}
