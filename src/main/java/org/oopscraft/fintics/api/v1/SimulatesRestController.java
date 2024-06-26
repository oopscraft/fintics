package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.SimulateRequest;
import org.oopscraft.fintics.api.v1.dto.SimulateResponse;
import org.oopscraft.fintics.api.v1.dto.StrategyRequest;
import org.oopscraft.fintics.api.v1.dto.TradeRequest;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.SimulateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/simulates")
@PreAuthorize("hasAuthority('API_SIMULATES')")
@RequiredArgsConstructor
@Tag(name = "simulate", description = "Simulates")
public class SimulatesRestController {

    private final SimulateService simulateService;

    @GetMapping
    public ResponseEntity<List<SimulateResponse>> getSimulates(
            @RequestParam(value = "tradeId", required = false) String tradeId,
            @RequestParam(value = "status", required = false) Simulate.Status status,
            @RequestParam(value = "favorite", required = false) Boolean favorite,
            @PageableDefault Pageable pageable
    ){
        SimulateSearch simulateSearch = SimulateSearch.builder()
                .tradeId(tradeId)
                .status(status)
                .favorite(favorite)
                .build();
        Page<Simulate> simulatePage = simulateService.getSimulates(simulateSearch, pageable);
        List<SimulateResponse> simulateResponses = simulatePage.getContent().stream()
                .map(SimulateResponse::from)
                .toList();
        long count = simulatePage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("simulate", pageable, count))
                .body(simulateResponses);
    }

    @GetMapping("{simulateId}")
    public ResponseEntity<SimulateResponse> getSimulate(@PathVariable("simulateId") String simulateId) {
        SimulateResponse simulateResponse = simulateService.getSimulate(simulateId)
                .map(SimulateResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(simulateResponse);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    public ResponseEntity<SimulateResponse> runSimulate(@RequestBody SimulateRequest simulateRequest) {
        // trade
        TradeRequest tradeRequest = simulateRequest.getTrade();
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .tradeName(tradeRequest.getTradeName())
                .interval(tradeRequest.getInterval())
                .threshold(tradeRequest.getThreshold())
                .startAt(tradeRequest.getStartAt())
                .endAt(tradeRequest.getEndAt())
                .strategyVariables(tradeRequest.getStrategyVariables())
                .build();
        List<TradeAsset> tradeAssets = tradeRequest.getTradeAssets().stream()
                .map(tradeAssetResponse -> TradeAsset.builder()
                        .tradeId(tradeRequest.getTradeId())
                        .assetId(tradeAssetResponse.getAssetId())
                        .assetName(tradeAssetResponse.getAssetName())
                        .enabled(tradeAssetResponse.isEnabled())
                        .holdingWeight(tradeAssetResponse.getHoldingWeight())
                        .build())
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        // strategy
        StrategyRequest strategyRequest = simulateRequest.getStrategy();
        Strategy strategy = Strategy.builder()
                .strategyId(strategyRequest.getStrategyId())
                .strategyName(strategyRequest.getStrategyName())
                .variables(strategyRequest.getVariables())
                .language(strategyRequest.getLanguage())
                .script(strategyRequest.getScript())
                .build();

        LocalDateTime dateTimeFrom = simulateRequest.getDateTimeFrom()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime dateTimeTo = simulateRequest.getDateTimeTo()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        BigDecimal investAmount = simulateRequest.getInvestAmount();
        BigDecimal feeRate = simulateRequest.getFeeRate();
        Simulate simulate = Simulate.builder()
                .trade(trade)
                .strategy(strategy)
                .tradeId(trade.getTradeId())
                .tradeName(trade.getTradeName())
                .dateTimeFrom(dateTimeFrom)
                .dateTimeTo(dateTimeTo)
                .investAmount(investAmount)
                .feeRate(feeRate)
                .build();
        simulate = simulateService.runSimulate(simulate);
        SimulateResponse simulateResponse = SimulateResponse.from(simulate);
        return ResponseEntity.ok(simulateResponse);
    }

    @PutMapping("{simulateId}/stop")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    public ResponseEntity<Void> stopSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.stopSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{simulateId}")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    public ResponseEntity<SimulateResponse> modifySimulate(@PathVariable("simulateId") String simulateId, @RequestBody SimulateRequest simulateRequest) {
        Simulate simulate = simulateService.getSimulate(simulateId).orElseThrow();
        if (simulateRequest.getFavorite() != null) {
            simulate.setFavorite(simulateRequest.getFavorite());
        }
        if (simulateRequest.getStatus() != null) {
            simulate.setStatus(simulateRequest.getStatus());
        }
        Simulate savedSimulate = simulateService.modifySimulate(simulate);
        return ResponseEntity.ok(SimulateResponse.from(savedSimulate));
    }

    @DeleteMapping("{simulateId}")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    public ResponseEntity<Void> deleteSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.deleteSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

}
