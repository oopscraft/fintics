package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "simulates", description = "simulate operations")
public class SimulatesRestController {

    private final SimulateService simulateService;

    /**
     * get list of simulate
     * @param tradeId trade id
     * @param status status
     * @param favorite favorite
     * @param pageable pageable
     * @return list of simulate
     */
    @GetMapping
    @Operation(description = "gets simulates")
    public ResponseEntity<List<SimulateResponse>> getSimulates(
            @RequestParam(value = "tradeId", required = false)
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

    /**
     * gets specified simulate
     * @param simulateId simulate id
     * @return simulate info
     */
    @GetMapping("{simulateId}")
    @Operation(description = "get specified simulate")
    public ResponseEntity<SimulateResponse> getSimulate(
            @PathVariable("simulateId")
            @Parameter(description = "simulate id")
                    String simulateId
    ) {
        SimulateResponse simulateResponse = simulateService.getSimulate(simulateId)
                .map(SimulateResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(simulateResponse);
    }

    /**
     * runs simulate
     * @param simulateRequest simulate request
     * @return running simulate
     */
    @PostMapping
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    @Operation(description = "run simulate")
    public ResponseEntity<SimulateResponse> runSimulate(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "simulate info to run")
                    SimulateRequest simulateRequest
    ) {
        // trade
        TradeRequest tradeRequest = simulateRequest.getTrade();
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .tradeName(tradeRequest.getTradeName())
                .interval(tradeRequest.getInterval())
                .threshold(tradeRequest.getThreshold())
                .startTime(tradeRequest.getStartAt())
                .endTime(tradeRequest.getEndAt())
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

        BigDecimal investAmount = simulateRequest.getInvestAmount();
        BigDecimal feeRate = simulateRequest.getFeeRate();
        Simulate simulate = Simulate.builder()
                .trade(trade)
                .strategy(strategy)
                .tradeId(trade.getTradeId())
                .tradeName(trade.getTradeName())
                .investFrom(simulateRequest.getInvestFrom())
                .investTo(simulateRequest.getInvestTo())
                .investAmount(investAmount)
                .feeRate(feeRate)
                .build();
        simulate = simulateService.runSimulate(simulate);
        SimulateResponse simulateResponse = SimulateResponse.from(simulate);
        return ResponseEntity.ok(simulateResponse);
    }

    /**
     * stops simulate
     * @param simulateId simulate id
     * @return void
     */
    @PutMapping("{simulateId}/stop")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    @Operation(description = "stop simulate")
    public ResponseEntity<Void> stopSimulate(
            @PathVariable("simulateId")
            @Parameter(description = "simulate id")
                    String simulateId
    ) {
        simulateService.stopSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

    /**
     * modified specified simulate info
     * @param simulateId simulate id
     * @param simulateRequest modified simulate info
     * @return modified simulate info
     */
    @PutMapping("{simulateId}")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    @Operation(description = "modifies simulate")
    public ResponseEntity<SimulateResponse> modifySimulate(
            @PathVariable("simulateId")
            @Parameter(description = "simulate id")
                    String simulateId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "simulate info")
                    SimulateRequest simulateRequest
    ) {
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

    /**
     * deletes simulate info
     * @param simulateId simulate id
     * @return void
     */
    @DeleteMapping("{simulateId}")
    @PreAuthorize("hasAuthority('API_SIMULATES_EDIT')")
    @Transactional
    @Operation(description = "deletes simulate")
    public ResponseEntity<Void> deleteSimulate(
            @PathVariable("simulateId")
            @Parameter(description = "simulate id")
                    String simulateId
    ) {
        simulateService.deleteSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

}
