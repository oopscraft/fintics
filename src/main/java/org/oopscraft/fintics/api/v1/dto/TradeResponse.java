package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeResponse {

    private String tradeId;

    private String name;

    private boolean enabled;

    private Integer interval;

    private String clientType;

    private String clientProperties;

    private String buyRule;

    private String sellRule;

    @Builder.Default
    private List<TradeAssetResponse> tradeAssets = new ArrayList<>();

    public static TradeResponse from(Trade trade) {
        TradeResponse tradeResponse = TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .name(trade.getName())
                .enabled(trade.isEnabled())
                .interval(trade.getInterval())
                .clientType(trade.getClientType())
                .clientProperties(trade.getClientProperties())
                .buyRule(trade.getBuyRule())
                .sellRule(trade.getSellRule())
                .build();
        List<TradeAssetResponse> tradeAssetResponses = trade.getTradeAssets().stream()
                .map(TradeAssetResponse::from)
                .collect(Collectors.toList());
        tradeResponse.setTradeAssets(tradeAssetResponses);
        return tradeResponse;
    }

}
