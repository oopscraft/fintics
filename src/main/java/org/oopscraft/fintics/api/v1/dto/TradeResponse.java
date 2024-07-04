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

    private String strategyId;

    private String strategyVariables;

    private Order.Kind orderKind;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    @Builder.Default
    private List<TradeAssetResponse> tradeAssets = new ArrayList<>();

    public static TradeResponse from(Trade trade) {
        TradeResponse tradeResponse = TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .tradeName(trade.getTradeName())
                .enabled(trade.isEnabled())
                .interval(trade.getInterval())
                .threshold(trade.getThreshold())
                .startAt(trade.getStartTime())
                .endAt(trade.getEndTime())
                .investAmount(trade.getInvestAmount())
                .brokerId(trade.getBrokerId())
                .strategyId(trade.getStrategyId())
                .strategyVariables(trade.getStrategyVariables())
                .orderKind(trade.getOrderKind())
                .alarmId(trade.getAlarmId())
                .alarmOnError(trade.isAlarmOnError())
                .alarmOnOrder(trade.isAlarmOnOrder())
                .build();

        // trade asset
        List<TradeAssetResponse> tradeAssetResponses = trade.getTradeAssets().stream()
                .map(TradeAssetResponse::from)
                .collect(Collectors.toList());
        tradeResponse.setTradeAssets(tradeAssetResponses);

        // returns
        return tradeResponse;
    }

}
