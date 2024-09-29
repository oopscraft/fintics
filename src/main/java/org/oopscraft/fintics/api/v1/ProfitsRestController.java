package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.ProfitResponse;
import org.oopscraft.fintics.model.Profit;
import org.oopscraft.fintics.service.ProfitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profits")
@PreAuthorize("hasAuthority('api.profits')")
@RequiredArgsConstructor
public class ProfitsRestController {

    private final ProfitService realizedProfitService;

    /**
     * gets trade realized profits
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return realized profit
     */
    @GetMapping("{brokerId}")
    @Operation(description = "gets realized profits")
    public ResponseEntity<ProfitResponse> getRealizedProfits(
            @PathVariable("brokerId") String brokerId,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo
    ) {
        Profit realizedProfit = realizedProfitService.getProfit(brokerId, dateFrom, dateTo);
        ProfitResponse realizedProfitResponse = ProfitResponse.from(realizedProfit);
        return ResponseEntity.ok(realizedProfitResponse);
    }

}
