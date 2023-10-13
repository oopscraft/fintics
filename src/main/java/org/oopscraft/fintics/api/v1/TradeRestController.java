package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.AssetIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.BalanceResponse;
import org.oopscraft.fintics.api.v1.dto.TradeRequest;
import org.oopscraft.fintics.api.v1.dto.TradeResponse;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
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
    @PreAuthorize("hasAuthority('TRADE_EDIT')")
    public ResponseEntity<TradeResponse> createTrade(@RequestBody TradeRequest tradeRequest) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .name(tradeRequest.getName())
                .enabled(tradeRequest.isEnabled())
                .interval(tradeRequest.getInterval())
                .startAt(tradeRequest.getStartAt())
                .endAt(tradeRequest.getEndAt())
                .clientType(tradeRequest.getClientType())
                .clientProperties(tradeRequest.getClientProperties())
                .holdCondition(tradeRequest.getHoldCondition())
                .alarmId(tradeRequest.getAlarmId())
                .alarmOnError(tradeRequest.isAlarmOnError())
                .alarmOnOrder(tradeRequest.isAlarmOnOrder())
                .build();

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .type(tradeAssetRequest.getType())
                                .enabled(tradeAssetRequest.isEnabled())
                                .holdRatio(tradeAssetRequest.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        trade = tradeService.saveTrade(trade);

        TradeResponse tradeResponse = TradeResponse.from(trade);
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeResponse);
    }

    @PutMapping("{tradeId}")
    @Transactional
    @PreAuthorize("hasAuthority('TRADE_EDIT')")
    public ResponseEntity<TradeResponse> modifyTrade(
            @PathVariable("tradeId")String tradeId,
            @RequestBody TradeRequest tradeRequest
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        trade.setName(tradeRequest.getName());
        trade.setEnabled(tradeRequest.isEnabled());
        trade.setInterval(tradeRequest.getInterval());
        trade.setStartAt(tradeRequest.getStartAt());
        trade.setEndAt(tradeRequest.getEndAt());
        trade.setClientType(tradeRequest.getClientType());
        trade.setClientProperties(tradeRequest.getClientProperties());
        trade.setHoldCondition(tradeRequest.getHoldCondition());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .type(tradeAssetRequest.getType())
                                .enabled(tradeAssetRequest.isEnabled())
                                .holdRatio(tradeAssetRequest.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        trade = tradeService.saveTrade(trade);

        TradeResponse tradeResponse = TradeResponse.from(trade);
        return ResponseEntity.ok(tradeResponse);
    }

    @GetMapping("{tradeId}/balance")
    public ResponseEntity<BalanceResponse> getTradeBalance(@PathVariable("tradeId") String tradeId) {
        BalanceResponse balanceResponse = tradeService.getTradeBalance(tradeId)
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

    @GetMapping("{tradeId}/asset-indicator")
    public ResponseEntity<List<AssetIndicatorResponse>> getTradeAssetIndicator(@PathVariable("tradeId") String tradeId) {
        List<AssetIndicatorResponse> assetIndicatorResponses = tradeService.getTradeAssetIndicator(tradeId).stream()
                .map(AssetIndicatorResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assetIndicatorResponses);
    }

}
