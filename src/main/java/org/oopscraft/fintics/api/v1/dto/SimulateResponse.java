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

    private TradeResponse trade;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal feeRate;

    private String holdCondition;

    private String result;

    private BalanceResponse balance;

    public static SimulateResponse from(Simulate simulate) {
        return SimulateResponse.builder()
                .simulateId(simulate.getSimulateId())
                .startedAt(simulate.getStartedAt())
                .endedAt(simulate.getEndedAt())
                .status(simulate.getStatus())
                .tradeId(simulate.getTradeId())
                .tradeName(simulate.getTradeName())
                .trade(TradeResponse.from(simulate.getTrade()))
                .dateTimeFrom(simulate.getDateTimeFrom())
                .dateTimeTo(simulate.getDateTimeTo())
                .investAmount(simulate.getInvestAmount())
                .feeRate(simulate.getFeeRate())
                .balance(BalanceResponse.from(simulate.getBalance()))
                .build();
    }

}
