package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.SimulateRequest;
import org.oopscraft.fintics.api.v1.dto.SimulateResponse;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.SimulateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SIMULATE')")
@Tag(name = "simulate", description = "Simulates trade")
public class SimulateRestController {

    private final SimulateService simulateService;

    @GetMapping
    public ResponseEntity<List<SimulateResponse>> getSimulates(
            SimulateSearch simulateSearch,
            @PageableDefault Pageable pageable
    ){
        Page<Simulate> simulatePage = simulateService.getSimulates(simulateSearch, pageable);
        List<SimulateResponse> simulateResponses = simulatePage.getContent().stream()
                .map(SimulateResponse::from)
                .toList();
        long count = simulatePage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("simulate", pageable, count))
                .body(simulateResponses);
    }

    @PostMapping
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
    public ResponseEntity<Void> stopSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.stopSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

}
