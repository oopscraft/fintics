package org.oopscraft.fintics.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.security.SecurityUtils;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.api.v1.dto.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.AssetService;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.TradeService;
import org.oopscraft.fintics.trade.TradeThreadManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
@Slf4j
public class TradeRestController {

    private final TradeService tradeService;

    private final TradeThreadManager tradeThreadManager;

    private final ObjectMapper objectMapper;

    private final IndiceService indiceService;

    private final AssetService assetService;

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
                .userId(SecurityUtils.getCurrentUserId())
                .publicEnabled(tradeRequest.isPublicEnabled())
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
        trade.setStartAt(tradeRequest.getStartAt());
        trade.setEndAt(tradeRequest.getEndAt());
        trade.setClientType(tradeRequest.getClientType());
        trade.setClientProperties(tradeRequest.getClientProperties());
        trade.setHoldCondition(tradeRequest.getHoldCondition());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());
        trade.setPublicEnabled(tradeRequest.isPublicEnabled());

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

    @GetMapping("{tradeId}/indicator")
    public ResponseEntity<List<AssetIndicatorResponse>> getTradeAssetIndicators(@PathVariable("tradeId") String tradeId) throws InterruptedException {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        List<AssetIndicatorResponse> tradeAssetIndicatorResponses = new ArrayList<>();
        for(TradeAsset tradeAsset : trade.getTradeAssets()) {
            AssetIndicator assetIndicator = tradeService.getTradeAssetIndicator(tradeId, tradeAsset.getSymbol()).orElseThrow();
            tradeAssetIndicatorResponses.add(AssetIndicatorResponse.from(assetIndicator));
        }
        return ResponseEntity.ok(tradeAssetIndicatorResponses);
    }

    @GetMapping(value = "{tradeId}/log", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getTradeLog(@PathVariable("tradeId")String tradeId) {
        SseLogAppender sseLogAppender = tradeThreadManager.getSseLogAppender(tradeId).orElseThrow();
        SseEmitter sseEmitter = new SseEmitter(60_000L);
        sseLogAppender.addSseEmitter(sseEmitter);
        sseEmitter.onCompletion(() -> sseLogAppender.removeSseEmitter(sseEmitter));
        sseEmitter.onTimeout(() -> sseLogAppender.removeSseEmitter(sseEmitter));
        return sseEmitter;
    }

    @GetMapping(value = "{tradeId}/order")
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @PathVariable("tradeId")
                    String tradeId,
            @PageableDefault
                    Pageable pageable
    ) {
        Page<Order> orderPage = tradeService.getTradeOrders(tradeId, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        // set trade name
        orderResponses.forEach(orderResponse -> {
            orderResponse.setTradeName(tradeService.getTrade(orderResponse.getTradeId())
                    .map(Trade::getName)
                    .orElse(""));
        });

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("order", pageable, orderPage.getTotalElements()))
                .body(orderResponses);
    }

}
