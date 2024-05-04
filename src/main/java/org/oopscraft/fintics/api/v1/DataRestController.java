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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "data", description = "Data")
public class DataRestController {

    private static final List<AssetOhlcvSummaryResponse> assetOhlcvSummaryResponses = new CopyOnWriteArrayList<>();

    private static CompletableFuture<Void> assetOhlcvSummaryResponsesFuture = new CompletableFuture<>();

    private static final List<IndiceOhlcvSummaryResponse> indiceOhlcvSummaryResponses = new CopyOnWriteArrayList<>();

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
    public ResponseEntity<List<AssetOhlcvSummaryResponse>> getAssetOhlcvSummaries() {
        if (assetOhlcvSummaryResponses.isEmpty()) {
            assetOhlcvSummaryResponses.addAll(dataService.getAssetOhlcvSummaries().stream()
                    .map(AssetOhlcvSummaryResponse::from)
                    .toList());
            return ResponseEntity.ok(assetOhlcvSummaryResponses);
        } else {
            if (assetOhlcvSummaryResponsesFuture.isDone()) {
                assetOhlcvSummaryResponsesFuture = CompletableFuture.runAsync(() -> {
                    assetOhlcvSummaryResponses.clear();
                    assetOhlcvSummaryResponses.addAll(dataService.getAssetOhlcvSummaries().stream()
                            .map(AssetOhlcvSummaryResponse::from)
                            .toList());
                });
            }
            return ResponseEntity.ok(assetOhlcvSummaryResponses);
        }
    }

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<AssetOhlcvResponse>> getAssetOhlcvs(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            @RequestParam(value = "interpolated", required = false) Boolean interpolated,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        List<AssetOhlcvResponse> assetOhlcvResponses = dataService.getAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, interpolated, pageable).stream()
                .map(AssetOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-ohlcvs", pageable))
                .body(assetOhlcvResponses);
    }

    @GetMapping("indice-ohlcv-summaries")
    public ResponseEntity<List<IndiceOhlcvSummaryResponse>> getIndiceOhlcvSummaries() {
        if (indiceOhlcvSummaryResponses.isEmpty()) {
            indiceOhlcvSummaryResponses.addAll(dataService.getIndiceOhlcvSummaries().stream()
                    .map(IndiceOhlcvSummaryResponse::from)
                    .toList());
            return ResponseEntity.ok(indiceOhlcvSummaryResponses);
        } else {
            if (indiceOhlcvSummaryResponsesFuture.isDone()) {
                indiceOhlcvSummaryResponsesFuture = CompletableFuture.runAsync(() -> {
                    indiceOhlcvSummaryResponses.clear();
                    indiceOhlcvSummaryResponses.addAll(dataService.getIndiceOhlcvSummaries().stream()
                            .map(IndiceOhlcvSummaryResponse::from)
                            .toList());
                });
            }
            return ResponseEntity.ok(indiceOhlcvSummaryResponses);
        }
    }

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<IndiceOhlcvResponse>> getIndiceOhlcvs(
            @RequestParam(value = "indiceId", required = false) Indice.Id indiceId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            @RequestParam(value = "interpolated", required = false) Boolean interpolated,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        List<IndiceOhlcvResponse> indiceOhlcvResponses = dataService.getIndiceOhlcvs(indiceId, type, dateTimeFrom, dateTimeTo, interpolated, pageable).stream()
                .map(IndiceOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

}
