package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableAsQueryParam;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.*;
import org.oopscraft.fintics.model.*;
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
@PreAuthorize("hasAuthority('API_TRADES')")
@RequiredArgsConstructor
@Tag(name = "trade", description = "trade operations")
@Slf4j
public class TradesRestController {

    private final TradeService tradeService;

    /**
     * gets trades
     * @return list of trade
     */
    @GetMapping
    @Operation(description = "gets trades")
    public ResponseEntity<List<TradeResponse>> getTrades() {
        List<TradeResponse> tradeResponses = tradeService.getTrades().stream()
                .map(TradeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tradeResponses);
    }

    /**
     * gets trade
     * @param tradeId trade id
     * @return trade info
     */
    @GetMapping("{tradeId}")
    @Operation(description = "gets trade")
    public ResponseEntity<TradeResponse> getTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        TradeResponse tradeResponse = tradeService.getTrade(tradeId)
                .map(TradeResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(tradeResponse);
    }

    /**
     * creates trade
     * @param tradeRequest trade request
     * @return created trade info
     */
    @PostMapping
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    @Transactional
    @Operation(description = "creates trade")
    public ResponseEntity<TradeResponse> createTrade(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "trade request")
                    TradeRequest tradeRequest
    ) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .tradeName(tradeRequest.getTradeName())
                .enabled(tradeRequest.isEnabled())
                .interval(tradeRequest.getInterval())
                .threshold(tradeRequest.getThreshold())
                .startTime(tradeRequest.getStartAt())
                .endTime(tradeRequest.getEndAt())
                .investAmount(tradeRequest.getInvestAmount())
                .brokerId(tradeRequest.getBrokerId())
                .basketId(tradeRequest.getBasketId())
                .strategyId(tradeRequest.getStrategyId())
                .strategyVariables(tradeRequest.getStrategyVariables())
                .orderKind(tradeRequest.getOrderKind())
                .alarmId(tradeRequest.getAlarmId())
                .alarmOnError(tradeRequest.isAlarmOnError())
                .alarmOnOrder(tradeRequest.isAlarmOnOrder())
                .build();
        Trade savedTrade = tradeService.saveTrade(trade);
        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTradeResponse);
    }

    /**
     * modifies trade
     * @param tradeId trade id
     * @param tradeRequest trade info
     * @return modified trade info
     */
    @PutMapping("{tradeId}")
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    @Transactional
    @Operation(description = "modifies trade")
    public ResponseEntity<TradeResponse> modifyTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "trade request")
                    TradeRequest tradeRequest
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        trade.setTradeName(tradeRequest.getTradeName());
        trade.setEnabled(tradeRequest.isEnabled());
        trade.setInterval(tradeRequest.getInterval());
        trade.setThreshold(tradeRequest.getThreshold());
        trade.setStartTime(tradeRequest.getStartAt());
        trade.setEndTime(tradeRequest.getEndAt());
        trade.setInvestAmount(tradeRequest.getInvestAmount());
        trade.setBrokerId(tradeRequest.getBrokerId());
        trade.setBasketId(tradeRequest.getBasketId());
        trade.setStrategyId(tradeRequest.getStrategyId());
        trade.setStrategyVariables(tradeRequest.getStrategyVariables());
        trade.setOrderKind(tradeRequest.getOrderKind());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());
        Trade savedTrade = tradeService.saveTrade(trade);
        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.ok(savedTradeResponse);
    }

    /**
     * deletes trade
     * @param tradeId trade id
     * @return void
     */
    @DeleteMapping("{tradeId}")
    @PreAuthorize("hasAuthority('API_TRADES_EDIT')")
    @Transactional
    @Operation(description = "deletes trade")
    public ResponseEntity<Void> deleteTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        tradeService.deleteTrade(tradeId);
        return ResponseEntity.ok().build();
    }

    /**
     * get trade profiles
     * @param tradeId trade id
     * @return profiles
     */
    @GetMapping("{tradeId}/assets")
    public ResponseEntity<List<TradeAssetResponse>> getAssets(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        List<TradeAssetResponse> tradeAssetResponses = tradeService.getTradeAssets(tradeId).stream()
                .map(TradeAssetResponse::from)
                .toList();
        return ResponseEntity.ok(tradeAssetResponses);
    }

    /**
     * gets trade orders
     * @param tradeId trade id
     * @param assetId asset id
     * @param type type
     * @param result result
     * @param pageable pageable
     * @return list of orders
     */
    @GetMapping("{tradeId}/orders")
    @Operation(description = "gets trade orders")
    @PageableAsQueryParam
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestParam(value = "assetId", required = false)
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "type", required = false)
            @Parameter(description = "type")
                    Order.Type type,
            @RequestParam(value = "result", required = false)
            @Parameter(description = "result")
                    Order.Result result,
            @PageableDefault
            @Parameter(hidden = true)
                    Pageable pageable
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

    /**
     * gets trade balance
     * @param tradeId trade id
     * @return balance
     */
    @GetMapping("{tradeId}/balance")
    @Operation(description = "gets trade balance")
    public ResponseEntity<BalanceResponse> getTradeBalance(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) throws InterruptedException {
        BalanceResponse balanceResponse = tradeService.getBalance(tradeId)
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

    /**
     * gets trade simulates
     * @param tradeId trade id
     * @param status status
     * @param favorite favorite
     * @param pageable pageable
     * @return list of simulate
     */
    @GetMapping("{tradeId}/simulates")
    @Operation(description = "gets trade simulates")
    public ResponseEntity<List<SimulateResponse>> getTradeSimulates(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestParam(value = "status", required = false)
            @Parameter(description = "status")
                    Simulate.Status status,
            @RequestParam(value = "favorite", required = false)
            @Parameter(description = "favorite")
                    Boolean favorite,
            @PageableDefault
            @Parameter(hidden = true)
                    Pageable pageable
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
