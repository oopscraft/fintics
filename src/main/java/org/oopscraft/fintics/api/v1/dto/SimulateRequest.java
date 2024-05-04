package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SimulateRequest {

    private TradeRequest trade;

    private StrategyRequest strategy;

    private ZonedDateTime dateTimeFrom;

    private ZonedDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal minimumOrderQuantity;

    private BigDecimal feeRate;

    private Simulate.Status status;

    private Boolean favorite;

}
