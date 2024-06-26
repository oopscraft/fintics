package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.SimulateReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private StrategyResponse strategy;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal feeRate;

    private String holdCondition;

    private LocalDateTime dateTime;

    private boolean favorite;

    private BigDecimal totalAmount;

    private BigDecimal profitAmount;

    private BigDecimal profitPercentage;

    private BalanceResponse balance;

    @Builder.Default
    private List<OrderResponse> orders = new ArrayList<>();

    private SimulateReportResponse simulateReport;

    public static SimulateResponse from(Simulate simulate) {
        return SimulateResponse.builder()
                .simulateId(simulate.getSimulateId())
                .startedAt(simulate.getStartedAt())
                .endedAt(simulate.getEndedAt())
                .status(simulate.getStatus())
                .tradeId(simulate.getTradeId())
                .tradeName(simulate.getTradeName())
                .trade(TradeResponse.from(simulate.getTrade()))
                .strategy(StrategyResponse.from(simulate.getStrategy()))
                .dateTimeFrom(simulate.getDateTimeFrom())
                .dateTimeTo(simulate.getDateTimeTo())
                .investAmount(simulate.getInvestAmount())
                .feeRate(simulate.getFeeRate())
                .dateTime(simulate.getDateTime())
                .favorite(simulate.isFavorite())
                .totalAmount(simulate.getTotalAmount())
                .profitAmount(simulate.getProfitAmount())
                .profitPercentage(simulate.getProfitPercentage())
                .balance(BalanceResponse.from(simulate.getBalance()))
                .orders(simulate.getOrders().stream()
                        .map(OrderResponse::from)
                        .toList())
                .simulateReport(SimulateReportResponse.from(simulate.getSimulateReport()))
                .build();
    }

}
