package org.oopscraft.fintics.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "simulate request")
public class SimulateRequest {

    @Schema(description = "trade")
    private TradeRequest trade;

    @Schema(description = "strategy")
    private StrategyRequest strategy;

    @Schema(description = "invest from")
    private LocalDateTime investFrom;

    @Schema(description = "invest to")
    private LocalDateTime investTo;

    @Schema(description = "invest amount")
    private BigDecimal investAmount;

    @Schema(description = "fee rate", example = "0.02")
    private BigDecimal feeRate;

    @Schema(description = "status")
    private Simulate.Status status;

    @Schema(description = "favorite")
    private Boolean favorite;

}
