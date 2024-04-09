package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.SimulateService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('API_TRADES')")
@Tag(name = "trade", description = "Trade operations")
@Slf4j
public class TradesRestController {

    private final static String TRADE_REST_CONTROLLER_GET_TRADE_ASSET_INDICATORS = "TradeRestController.getTradeAssetIndicator";

    private final TradeService tradeService;

    private final SimulateService simulateService;

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
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    public ResponseEntity<TradeResponse> createTrade(@RequestBody TradeRequest tradeRequest) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .tradeName(tradeRequest.getTradeName())
                .enabled(tradeRequest.isEnabled())
                .interval(tradeRequest.getInterval())
                .threshold(tradeRequest.getThreshold())
                .startAt(tradeRequest.getStartAt())
                .endAt(tradeRequest.getEndAt())
                .brokerId(tradeRequest.getBrokerId())
                .ruleId(tradeRequest.getRuleId())
                .ruleConfig(tradeRequest.getRuleConfig())
                .orderKind(tradeRequest.getOrderKind())
                .alarmId(tradeRequest.getAlarmId())
                .alarmOnError(tradeRequest.isAlarmOnError())
                .alarmOnOrder(tradeRequest.isAlarmOnOrder())
                .build();

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .assetId(tradeAssetRequest.getAssetId())
                                .assetName(tradeAssetRequest.getAssetName())
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
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    public ResponseEntity<TradeResponse> modifyTrade(
            @PathVariable("tradeId")String tradeId,
            @RequestBody TradeRequest tradeRequest
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        trade.setTradeName(tradeRequest.getTradeName());
        trade.setEnabled(tradeRequest.isEnabled());
        trade.setInterval(tradeRequest.getInterval());
        trade.setThreshold(tradeRequest.getThreshold());
        trade.setStartAt(tradeRequest.getStartAt());
        trade.setEndAt(tradeRequest.getEndAt());
        trade.setBrokerId(tradeRequest.getBrokerId());
        trade.setRuleId(tradeRequest.getRuleId());
        trade.setRuleConfig(tradeRequest.getRuleConfig());
        trade.setOrderKind(tradeRequest.getOrderKind());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());

        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetRequest ->
                        TradeAsset.builder()
                                .tradeId(tradeAssetRequest.getTradeId())
                                .assetId(tradeAssetRequest.getAssetId())
                                .assetName(tradeAssetRequest.getAssetName())
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
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    public ResponseEntity<Void> deleteTrade(@PathVariable("tradeId")String tradeId) {
        tradeService.deleteTrade(tradeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{tradeId}/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @PathVariable("tradeId")String tradeId,
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Order.Type type,
            @RequestParam(value = "result", required = false) Order.Result result,
            @PageableDefault Pageable pageable
    ) {
        Page<Order> orderPage = tradeService.getOrders(tradeId, assetId, type, result, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();
        long count = orderPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("orders", pageable, count))
                .body(orderResponses);
    }

    @GetMapping("{tradeId}/balance")
    public ResponseEntity<BalanceResponse> getTradeBalance(@PathVariable("tradeId") String tradeId) throws InterruptedException {
        BalanceResponse balanceResponse = tradeService.getBalance(tradeId)
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

    @GetMapping("{tradeId}/simulates")
    public ResponseEntity<List<SimulateResponse>> getTradeSimulates(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "status", required = false) Simulate.Status status,
            @RequestParam(value = "favorite", required = false) Boolean favorite,
            @PageableDefault Pageable pageable
    ){
        Page<Simulate> simulatePage = tradeService.getSimulates(tradeId, status, favorite, pageable);
        List<SimulateResponse> simulateResponses = simulatePage.getContent().stream()
                .map(SimulateResponse::from)
                .toList();
        long total = simulatePage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("simulates", pageable, total))
                .body(simulateResponses);
    }

}
