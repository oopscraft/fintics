package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.security.SecurityUtils;
import org.oopscraft.fintics.api.v1.dto.AssetIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.BalanceResponse;
import org.oopscraft.fintics.api.v1.dto.TradeRequest;
import org.oopscraft.fintics.api.v1.dto.TradeResponse;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.service.TradeService;
import org.oopscraft.fintics.trade.TradeThreadManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
@Tag(name = "trade", description = "Trade operations")
@Slf4j
public class TradeRestController {

    private final static String TRADE_REST_CONTROLLER_GET_TRADE_ASSET_INDICATORS = "TradeRestController.getTradeAssetIndicator";

    private final TradeService tradeService;

    private final TradeThreadManager tradeThreadManager;

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
                .threshold(tradeRequest.getThreshold())
                .startAt(tradeRequest.getStartAt())
                .endAt(tradeRequest.getEndAt())
                .clientId(tradeRequest.getClientId())
                .clientProperties(tradeRequest.getClientProperties())
                .holdCondition(tradeRequest.getHoldCondition())
                .orderKind(tradeRequest.getOrderKind())
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
                                .enabled(tradeAssetRequest.isEnabled())
                                .holdRatio(tradeAssetRequest.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        Trade savedTrade = tradeService.saveTrade(trade);
        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTradeResponse);
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
        trade.setThreshold(tradeRequest.getThreshold());
        trade.setStartAt(tradeRequest.getStartAt());
        trade.setEndAt(tradeRequest.getEndAt());
        trade.setClientId(tradeRequest.getClientId());
        trade.setClientProperties(tradeRequest.getClientProperties());
        trade.setHoldCondition(tradeRequest.getHoldCondition());
        trade.setOrderKind(tradeRequest.getOrderKind());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .symbol(tradeAssetRequest.getSymbol())
                                .name(tradeAssetRequest.getName())
                                .enabled(tradeAssetRequest.isEnabled())
                                .holdRatio(tradeAssetRequest.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        Trade savedTrade = tradeService.saveTrade(trade);

        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.ok(savedTradeResponse);
    }

    @DeleteMapping("{tradeId}")
    @Transactional
    @PreAuthorize("hasAuthority('TRADE_EDIT')")
    public ResponseEntity<Void> deleteTrade(@PathVariable("tradeId")String tradeId) {
        tradeService.deleteTrade(tradeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{tradeId}/balance")
    public ResponseEntity<BalanceResponse> getTradeBalance(@PathVariable("tradeId") String tradeId) throws InterruptedException {
        BalanceResponse balanceResponse = tradeService.getTradeBalance(tradeId)
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

    @Cacheable(cacheNames = TRADE_REST_CONTROLLER_GET_TRADE_ASSET_INDICATORS, key = "#tradeId")
    @GetMapping("{tradeId}/indicator")
    public ResponseEntity<List<AssetIndicatorResponse>> getTradeAssetIndicators(@PathVariable("tradeId") String tradeId) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        List<AssetIndicatorResponse> tradeAssetIndicatorResponses = new ArrayList<>();
        for(TradeAsset tradeAsset : trade.getTradeAssets()) {
            AssetIndicator assetIndicator = tradeService.getTradeAssetIndicator(tradeId, tradeAsset.getSymbol()).orElseThrow();
            tradeAssetIndicatorResponses.add(AssetIndicatorResponse.from(assetIndicator));
        }
        return ResponseEntity.ok(tradeAssetIndicatorResponses);
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    @PreAuthorize("permitAll()")
    @CacheEvict(cacheNames = TRADE_REST_CONTROLLER_GET_TRADE_ASSET_INDICATORS, allEntries = true)
    public void cacheEvictTradeAssetIndicators() {
        log.info("TradeRestController.cacheEvictTradeAssetIndicators");
    }

}
