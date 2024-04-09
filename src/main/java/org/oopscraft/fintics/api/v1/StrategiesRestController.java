package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.StrategyResponse;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.service.StrategyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/strategies")
@PreAuthorize("hasAuthority('API_STRATEGIES')")
@Tag(name = "strategies", description = "Strategies")
@RequiredArgsConstructor
public class StrategiesRestController {

    private final StrategyService strategyService;

    @GetMapping
    public ResponseEntity<List<StrategyResponse>> getStrategies(
            @RequestParam(value = "strategyName", required = false) String strategyName,
            @PageableDefault Pageable pageable
    ) {
        Page<Strategy> strategyPage = strategyService.getStrategies(strategyName, pageable);
        List<StrategyResponse> strategyResponses = strategyPage.getContent().stream()
                .map(StrategyResponse::from)
                .toList();
        long total = strategyPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("strategies", pageable, total))
                .body(strategyResponses);
    }

    @GetMapping("{strategyId}")
    public ResponseEntity<StrategyResponse> getStrategy(@PathVariable("strategyId")String strategyId) {
        StrategyResponse ruleResponse = strategyService.getStrategy(strategyId)
                .map(StrategyResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(ruleResponse);
    }

}
