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

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getAssetOhlcvs(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        List<OhlcvResponse> assetOhlcvResponses = dataService.getAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-ohlcvs", pageable))
                .body(assetOhlcvResponses);
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

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getIndiceOhlcvs(
            @RequestParam(value = "indiceId", required = false) Indice.Id indiceId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        List<OhlcvResponse> indiceOhlcvResponses = dataService.getIndiceOhlcvs(indiceId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

    @GetMapping("asset-news-summaries")
    public ResponseEntity<List<NewsSummaryResponse>> getAssetNewsSummaries() {
        List<NewsSummaryResponse> assetNewsSummaryResponses = dataService.getAssetNewsSummaries().stream()
                .map(NewsSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(assetNewsSummaryResponses);
    }

    @GetMapping("indice-news-summaries")
    public ResponseEntity<List<NewsSummaryResponse>> getIndiceNewsSummaries() {
        List<NewsSummaryResponse> indiceNewsSummaryResponses = dataService.getIndiceNewsSummaries().stream()
                .map(NewsSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(indiceNewsSummaryResponses);
    }

}
