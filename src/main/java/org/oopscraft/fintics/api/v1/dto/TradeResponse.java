package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.Trade;

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

    private String tradeClientId;

    private String tradeClientConfig;

    private String holdCondition;

    private String orderOperatorId;

    private OrderKind orderKind;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    private boolean publicEnabled;

    @Builder.Default
    private List<TradeAssetResponse> tradeAssets = new ArrayList<>();

    public static TradeResponse from(Trade trade) {
        TradeResponse tradeResponse = TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .tradeName(trade.getTradeName())
                .enabled(trade.isEnabled())
                .interval(trade.getInterval())
                .threshold(trade.getThreshold())
                .startAt(trade.getStartAt())
                .endAt(trade.getEndAt())
                .tradeClientId(trade.getTradeClientId())
                .tradeClientConfig(trade.getTradeClientConfig())
                .holdCondition(trade.getHoldCondition())
                .orderOperatorId(trade.getOrderOperatorId())
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
