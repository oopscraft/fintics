package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Trade;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeResponse {

    private String tradeId;

    private String tradeName;

    private boolean enabled;

    private Integer interval;

    private Integer threshold;

    private LocalTime startAt;

    private LocalTime endAt;

    private BigDecimal investAmount;

    private String brokerId;

    private String basketId;

    private String strategyId;

    private String strategyVariables;

    private Order.Kind orderKind;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .tradeName(trade.getTradeName())
                .enabled(trade.isEnabled())
                .interval(trade.getInterval())
                .threshold(trade.getThreshold())
                .startAt(trade.getStartTime())
                .endAt(trade.getEndTime())
                .investAmount(trade.getInvestAmount())
                .brokerId(trade.getBrokerId())
                .basketId(trade.getBasketId())
                .strategyId(trade.getStrategyId())
                .strategyVariables(trade.getStrategyVariables())
                .orderKind(trade.getOrderKind())
                .alarmId(trade.getAlarmId())
                .alarmOnError(trade.isAlarmOnError())
                .alarmOnOrder(trade.isAlarmOnOrder())
                .build();
    }

}
