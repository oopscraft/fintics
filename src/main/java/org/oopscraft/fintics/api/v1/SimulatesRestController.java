package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.SimulateRequest;
import org.oopscraft.fintics.api.v1.dto.SimulateResponse;
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

@RestController
@RequestMapping("/api/v1/simulates")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('API_SIMULATES')")
@Tag(name = "simulate", description = "Simulates trade")
public class SimulatesRestController {

    private final SimulateService simulateService;

    @GetMapping
    public ResponseEntity<List<SimulateResponse>> getSimulates(
            @RequestParam(value = "tradeId", required = false) String tradeId,
            @RequestParam(value = "status", required = false) Simulate.Status status,
            @RequestParam(value = "favorite", required = false) Boolean favorite,
            @PageableDefault Pageable pageable
    ){
        Page<Simulate> simulatePage = simulateService.getSimulates(tradeId, status, favorite, pageable);
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
    @Transactional
    public ResponseEntity<SimulateResponse> runSimulate(@RequestBody SimulateRequest simulateRequest) {
        Trade trade = simulateRequest.getTrade();
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
    @Transactional
    public ResponseEntity<Void> stopSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.stopSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{simulateId}")
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
    @Transactional
    public ResponseEntity<Void> deleteSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.deleteSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

}
