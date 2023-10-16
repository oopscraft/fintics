package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.TradeRequest;
import org.oopscraft.fintics.api.v1.dto.SimulateTradeResponse;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.model.SimulateTrade;
import org.oopscraft.fintics.service.SimulateTradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("simulate-trade")
@RequiredArgsConstructor
public class SimulateTradeRestController {

    private final SimulateTradeService tradeSimulateService;

    @PostMapping
    public ResponseEntity<SimulateTradeResponse> simulateTrade(TradeRequest tradeRequest) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .name(tradeRequest.getName())
                .clientType(tradeRequest.getClientType())
                .clientProperties(tradeRequest.getClientProperties())
                .tradeAssets(tradeRequest.getTradeAssets().stream()
                        .map(tradeAssetRequest -> TradeAsset.builder()
                                .tradeId(tradeRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .type(tradeAssetRequest.getType())
                                .build())
                        .collect(Collectors.toList()))
                .holdCondition(tradeRequest.getHoldCondition())
                .build();
        SimulateTrade simulateTrade = tradeSimulateService.simulateTrade(trade);
        return ResponseEntity.ok(SimulateTradeResponse.from(simulateTrade));
    }

}
