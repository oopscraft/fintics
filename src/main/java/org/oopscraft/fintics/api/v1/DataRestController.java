package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.DataService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "data", description = "Data")
public class DataRestController {

    private static final List<OhlcvSummaryResponse> assetOhlcvSummaryResponses = new CopyOnWriteArrayList<>();

    private static CompletableFuture<Void> assetOhlcvSummaryResponsesFuture = new CompletableFuture<>();

    private static final List<OhlcvSummaryResponse> indiceOhlcvSummaryResponses = new CopyOnWriteArrayList<>();

    private static CompletableFuture<Void> indiceOhlcvSummaryResponsesFuture = new CompletableFuture<>();

    private final DataService dataService;

    @GetMapping("assets")
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "market", required = false) String market,
            Pageable pageable
    ) {
        List<AssetResponse> assetResponses = dataService.getAssets(assetId, assetName, market, pageable).stream()
                .map(AssetResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("assets", pageable))
                .body(assetResponses);
    }

    @GetMapping("asset-ohlcv-summaries")
    public ResponseEntity<List<OhlcvSummaryResponse>> getAssetOhlcvSummaries() {
        if (assetOhlcvSummaryResponses.isEmpty()) {
            assetOhlcvSummaryResponses.addAll(dataService.getAssetOhlcvSummaries().stream()
                    .map(OhlcvSummaryResponse::from)
                    .toList());
            return ResponseEntity.ok(assetOhlcvSummaryResponses);
        } else {
            if (assetOhlcvSummaryResponsesFuture.isDone()) {
                assetOhlcvSummaryResponsesFuture = CompletableFuture.runAsync(() -> {
                    assetOhlcvSummaryResponses.clear();
                    assetOhlcvSummaryResponses.addAll(dataService.getAssetOhlcvSummaries().stream()
                            .map(OhlcvSummaryResponse::from)
                            .toList());
                });
            }
            return ResponseEntity.ok(assetOhlcvSummaryResponses);
        }
    }

    @GetMapping("asset-ohlcv-summaries/{assetId}")
    public ResponseEntity<OhlcvSummaryResponse> getAssetOhlcvSummary(@PathVariable("assetId") String assetId) {
        OhlcvSummaryResponse assetOhlcvSummaryResponse = dataService.getAssetOhlcvSummary(assetId)
                .map(OhlcvSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetOhlcvSummaryResponse);
    }

    @GetMapping("indice-ohlcv-summaries")
    public ResponseEntity<List<OhlcvSummaryResponse>> getIndiceOhlcvSummaries() {
        if (indiceOhlcvSummaryResponses.isEmpty()) {
            indiceOhlcvSummaryResponses.addAll(dataService.getIndiceOhlcvSummaries().stream()
                    .map(OhlcvSummaryResponse::from)
                    .toList());
            return ResponseEntity.ok(indiceOhlcvSummaryResponses);
        } else {
            if (indiceOhlcvSummaryResponsesFuture.isDone()) {
                indiceOhlcvSummaryResponsesFuture = CompletableFuture.runAsync(() -> {
                    indiceOhlcvSummaryResponses.clear();
                    indiceOhlcvSummaryResponses.addAll(dataService.getIndiceOhlcvSummaries().stream()
                            .map(OhlcvSummaryResponse::from)
                            .toList());
                });
            }
            return ResponseEntity.ok(indiceOhlcvSummaryResponses);
        }
    }

    @GetMapping("indice-ohlcv-summaries/{indiceId}")
    public ResponseEntity<OhlcvSummaryResponse> getIndiceOhlcvSummary(@PathVariable("indiceId") Indice.Id indiceId) {
        OhlcvSummaryResponse indiceOhlcvSummaryResponse = dataService.getIndiceOhlcvSummary(indiceId)
                .map(OhlcvSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceOhlcvSummaryResponse);
    }

    @GetMapping("asset-news-summaries")
    public ResponseEntity<List<NewsSummaryResponse>> getAssetNewsSummaries() {
        List<NewsSummaryResponse> assetNewsSummaryResponses = dataService.getAssetNewsSummaries().stream()
                .map(NewsSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(assetNewsSummaryResponses);
    }

    @GetMapping("asset-news-summaries/{assetId}")
    public ResponseEntity<NewsSummaryResponse> getAssetNewSummary(@PathVariable("assetId") String assetId) {
        NewsSummaryResponse newsSummaryResponse = dataService.getAssetNewsSummary(assetId)
                .map(NewsSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(newsSummaryResponse);
    }

    @GetMapping("indice-news-summaries")
    public ResponseEntity<List<NewsSummaryResponse>> getIndiceNewsSummaries() {
        List<NewsSummaryResponse> indiceNewsSummaryResponses = dataService.getIndiceNewsSummaries().stream()
                .map(NewsSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(indiceNewsSummaryResponses);
    }

    @GetMapping("indice-news-summaries/{indiceId}")
    public ResponseEntity<NewsSummaryResponse> getIndiceNewsSummary(@PathVariable("indiceId") Indice.Id indiceId) {
        NewsSummaryResponse newsSummaryResponse = dataService.getIndiceNewsSummary(indiceId)
                .map(NewsSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(newsSummaryResponse);
    }

}
