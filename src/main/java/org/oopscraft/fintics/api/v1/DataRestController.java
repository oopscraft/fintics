package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.common.data.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.*;
import org.oopscraft.fintics.service.DataService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "data", description = "Data")
public class DataRestController {

    private static final List<OhlcvSummaryResponse> ohlcvSummaryResponses = new CopyOnWriteArrayList<>();

    private static CompletableFuture<Void> ohlcvSummaryResponsesFuture = new CompletableFuture<>();

    private final DataService dataService;

    /**
     * gets assets
     * @param assetId asset id
     * @param name asset name
     * @param market market
     * @param pageable pageable
     * @return list of assets
     */
    @GetMapping("assets")
    @Operation(description = "gets assets")
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false)
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "name", required = false)
            @Parameter(description = "asset name")
                    String name,
            @RequestParam(value = "market", required = false)
            @Parameter(description = "market")
                    String market,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        List<AssetResponse> assetResponses = dataService.getAssets(assetId, name, market, pageable).stream()
                .map(AssetResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("assets", pageable))
                .body(assetResponses);
    }

    /**
     * gets ohlcv summary list
     * @return ohlcv summary list
     */
    @GetMapping("ohlcv-summaries")
    @Operation(description = "ohlcv summaries")
    public ResponseEntity<List<OhlcvSummaryResponse>> getOhlcvSummaries() {
        if (ohlcvSummaryResponses.isEmpty()) {
            ohlcvSummaryResponses.addAll(dataService.getOhlcvSummaries().stream()
                    .map(OhlcvSummaryResponse::from)
                    .toList());
            return ResponseEntity.ok(ohlcvSummaryResponses);
        } else {
            if (ohlcvSummaryResponsesFuture.isDone()) {
                ohlcvSummaryResponsesFuture = CompletableFuture.runAsync(() -> {
                    ohlcvSummaryResponses.clear();
                    ohlcvSummaryResponses.addAll(dataService.getOhlcvSummaries().stream()
                            .map(OhlcvSummaryResponse::from)
                            .toList());
                });
            }
            return ResponseEntity.ok(ohlcvSummaryResponses);
        }
    }

    /**
     * gets ohlcv summary info
     * @param assetId asset id
     * @return ohlcv summary
     */
    @GetMapping("ohlcv-summaries/{assetId}")
    @Operation(description = "gets ohlcv summaries")
    public ResponseEntity<OhlcvSummaryResponse> getOhlcvSummary(
            @PathVariable("assetId")
            @Parameter(description = "asset id")
                    String assetId
    ) {
        OhlcvSummaryResponse assetOhlcvSummaryResponse = dataService.getOhlcvSummary(assetId)
                .map(OhlcvSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetOhlcvSummaryResponse);
    }

}
