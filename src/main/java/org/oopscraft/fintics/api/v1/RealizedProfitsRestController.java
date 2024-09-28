package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.RealizedProfitResponse;
import org.oopscraft.fintics.model.RealizedProfit;
import org.oopscraft.fintics.service.RealizedProfitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/realized-profits")
@PreAuthorize("hasAuthority('api.realized-profits')")
@RequiredArgsConstructor
public class RealizedProfitsRestController {

    private final RealizedProfitService realizedProfitService;

    /**
     * gets trade realized profits
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return realized profits
     */
    @GetMapping("{brokerId}")
    @Operation(description = "gets realized profits")
    public ResponseEntity<List<RealizedProfitResponse>> getRealizedProfits(
            @PathVariable("brokerId") String brokerId,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo
    ) {
        List<RealizedProfit> realizedProfits = realizedProfitService.getRealizedProfits(brokerId, dateFrom, dateTo);
        List<RealizedProfitResponse> realizedProfitResponses = realizedProfits.stream()
                .map(RealizedProfitResponse::from)
                .toList();
        return ResponseEntity.ok(realizedProfitResponses);
    }

}
