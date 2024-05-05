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

    @GetMapping("asset-ohlcv-summaries/{assetId}")
    public ResponseEntity<AssetOhlcvSummaryResponse> getAssetOhlcvSummary(@PathVariable("assetId") String assetId) {
        AssetOhlcvSummaryResponse assetOhlcvSummaryResponse = dataService.getAssetOhlcvSummary(assetId)
                .map(AssetOhlcvSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetOhlcvSummaryResponse);
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

    @PostMapping("interpolate-asset-ohlcvs")
    @PreAuthorize("hasAuthority('API_DATA_EDIT')")
    public ResponseEntity<Void> interpolateAssetOhlcvs(@RequestBody Map<String, String> payload) {
        String assetId = payload.get("assetId");
        Ohlcv.Type type = Ohlcv.Type.valueOf(payload.get("type"));
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(payload.get("dateTimeFrom"));
        ZonedDateTime zonedDateTimeTo = ZonedDateTime.parse(payload.get("dateTimeTo"));
        LocalDateTime dateTimeFrom = zonedDateTimeFrom.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime dateTimeTo = zonedDateTimeTo.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        dataService.interpolateAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo);
        return ResponseEntity.ok().build();
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

    @GetMapping("indice-ohlcv-summaries/{indiceId}")
    public ResponseEntity<IndiceOhlcvSummaryResponse> getIndiceOhlcvSummary(@PathVariable("indiceId") Indice.Id indiceId) {
        IndiceOhlcvSummaryResponse indiceOhlcvSummaryResponse = dataService.getIndiceOhlcvSummary(indiceId)
                .map(IndiceOhlcvSummaryResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceOhlcvSummaryResponse);
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

    @PostMapping("interpolate-indice-ohlcvs")
    @PreAuthorize("hasAuthority('API_DATA_EDIT')")
    public ResponseEntity<Void> interpolateIndiceOhlcvs(@RequestBody Map<String, String> payload) {
        Indice.Id indiceId = Indice.Id.valueOf(payload.get("indiceId"));
        Ohlcv.Type type = Ohlcv.Type.valueOf(payload.get("type"));
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(payload.get("dateTimeFrom"));
        ZonedDateTime zonedDateTimeTo = ZonedDateTime.parse(payload.get("dateTimeTo"));
        LocalDateTime dateTimeFrom = zonedDateTimeFrom.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime dateTimeTo = zonedDateTimeTo.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        dataService.interpolateIndiceOhlcvs(indiceId, type, dateTimeFrom, dateTimeTo);
        return ResponseEntity.ok().build();
    }

}
