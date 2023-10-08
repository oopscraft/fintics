package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.TradeRequest;
import org.oopscraft.fintics.api.v1.dto.TradeResponse;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeRestController {

    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades() {
        List<TradeResponse> tradeResponses = tradeService.getTrades().stream()
                .map(TradeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tradeResponses);
    }

    @GetMapping("{tradeId}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable("tradeId")String tradeId) {
        TradeResponse tradeResponse = tradeService.getTrade(tradeId)
                .map(TradeResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(tradeResponse);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TradeResponse> createTrade(@RequestBody TradeRequest tradeRequest) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .name(tradeRequest.getName())
                .enabled(tradeRequest.isEnabled())
                .interval(tradeRequest.getInterval())
                .clientType(tradeRequest.getClientType())
                .clientProperties(tradeRequest.getClientProperties())
                .buyRule(tradeRequest.getBuyRule())
                .sellRule(tradeRequest.getSellRule())
                .build();

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .enabled(tradeAssetRequest.isEnabled())
                                .tradeRatio(tradeAssetRequest.getTradeRatio())
                                .limitRatio(tradeAssetRequest.getLimitRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        trade = tradeService.saveTrade(trade);

        TradeResponse tradeResponse = TradeResponse.from(trade);
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeResponse);
    }

    @PutMapping("{tradeId}")
    @Transactional
    public ResponseEntity<TradeResponse> modifyTrade(
            @PathVariable("tradeId")String tradeId,
            @RequestBody TradeRequest tradeRequest
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        trade.setName(tradeRequest.getName());
        trade.setEnabled(tradeRequest.isEnabled());
        trade.setInterval(tradeRequest.getInterval());
        trade.setClientType(tradeRequest.getClientType());
        trade.setClientProperties(tradeRequest.getClientProperties());
        trade.setBuyRule(tradeRequest.getBuyRule());
        trade.setSellRule(tradeRequest.getSellRule());

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .enabled(tradeAssetRequest.isEnabled())
                                .tradeRatio(tradeAssetRequest.getTradeRatio())
                                .limitRatio(tradeAssetRequest.getLimitRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        trade = tradeService.saveTrade(trade);

        TradeResponse tradeResponse = TradeResponse.from(trade);
        return ResponseEntity.ok(tradeResponse);
    }


}
