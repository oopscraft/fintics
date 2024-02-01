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

    private Trade trade;

    private ZonedDateTime dateTimeFrom;

    private ZonedDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal feeRate;

}
