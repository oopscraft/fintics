package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Simulate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class SimulateResponse {

    private String simulateId;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Simulate.Status status;

    private String tradeId;

    private String tradeName;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal feeRate;

    private String holdCondition;

    private String result;

    public static SimulateResponse from(Simulate simulate) {
        return SimulateResponse.builder()
                .simulateId(simulate.getSimulateId())
                .status(simulate.getStatus())
                .startedAt(simulate.getStartedAt())
                .endedAt(simulate.getEndedAt())
                .dateTimeFrom(simulate.getDateTimeFrom())
                .dateTimeTo(simulate.getDateTimeTo())
                .investAmount(simulate.getInvestAmount())
                .feeRate(simulate.getFeeRate())
                .build();
    }

}
