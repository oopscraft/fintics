package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.PrepareSimulateRequest;
import org.oopscraft.fintics.api.v1.dto.PrepareSimulateResponse;
import org.oopscraft.fintics.api.v1.dto.RunSimulateRequest;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.SimulateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/simulate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SIMULATE')")
@Tag(name = "simulate", description = "Simulates trade")
public class SimulateRestController {

    private final SimulateService simulateService;

    @PostMapping
    public ResponseEntity<PrepareSimulateResponse> prepareSimulate(@RequestBody PrepareSimulateRequest prepareSimulateRequest) {
        String simulateId = prepareSimulateRequest.getSimulateId();
        Trade trade = prepareSimulateRequest.getTrade();
        LocalDateTime dateTimeFrom = prepareSimulateRequest.getDateTimeFrom()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime dateTimeTo = prepareSimulateRequest.getDateTimeTo()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        // call service
        Simulate simulate = Simulate.builder()
                .simulateId(simulateId)
                .trade(trade)
                .dateTimeFrom(dateTimeFrom)
                .dateTimeTo(dateTimeTo)
                .build();
        simulate = simulateService.prepareSimulate(simulate);

        // response
        PrepareSimulateResponse prepareSimulateResponse = PrepareSimulateResponse.builder()
                .simulateId(simulate.getSimulateId())
                .indiceIndicators(simulate.getIndiceIndicators())
                .assetIndicators(simulate.getAssetIndicators())
                .build();
        return ResponseEntity.ok(prepareSimulateResponse);
    }

    @PutMapping("{simulateId}/run")
    public ResponseEntity<Void> runSimulate(
            @PathVariable("simulateId") String simulateId,
            @RequestBody RunSimulateRequest runSimulateRequest
    ) {
        Trade trade = runSimulateRequest.getTrade();
        LocalDateTime dateTimeFrom = runSimulateRequest.getDateTimeFrom()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime dateTimeTo = runSimulateRequest.getDateTimeTo()
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        BigDecimal investAmount = runSimulateRequest.getInvestAmount();
        BigDecimal feeRate = runSimulateRequest.getFeeRate();
        List<IndiceIndicator> indiceIndicators = runSimulateRequest.getIndiceIndicators();
        List<AssetIndicator> assetIndicators = runSimulateRequest.getAssetIndicators();

        Simulate simulate = Simulate.builder()
                .simulateId(simulateId)
                .trade(trade)
                .dateTimeFrom(dateTimeFrom)
                .dateTimeTo(dateTimeTo)
                .investAmount(investAmount)
                .feeRate(feeRate)
                .indiceIndicators(indiceIndicators)
                .assetIndicators(assetIndicators)
                .build();
        simulateService.runSimulate(simulate);

        return ResponseEntity.ok().build();
    }

    @PutMapping("{simulateId}/stop")
    public ResponseEntity<Void> stopSimulate(@PathVariable("simulateId") String simulateId) {
        simulateService.stopSimulate(simulateId);
        return ResponseEntity.ok().build();
    }

}
