package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.DataSummaryResponse;
import org.oopscraft.fintics.model.DataSummary;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.service.DataService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "data", description = "Data")
public class DataRestController {

    private final DataService dataService;

    @GetMapping("summary")
    public ResponseEntity<DataSummaryResponse> getSummary() {
        DataSummary dataSummary = dataService.getSummary();
        DataSummaryResponse dataSummaryResponse = DataSummaryResponse.from(dataSummary);
        return ResponseEntity.ok(dataSummaryResponse);
    }

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

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<DataSummaryResponse.AssetOhlcvResponse>> getAssetOhlcvs(
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
        List<DataSummaryResponse.AssetOhlcvResponse> assetOhlcvResponses = dataService.getAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, interpolated, pageable).stream()
                .map(DataSummaryResponse.AssetOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-ohlcvs", pageable))
                .body(assetOhlcvResponses);
    }

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<DataSummaryResponse.IndiceOhlcvResponse>> getIndiceOhlcvs(
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
        List<DataSummaryResponse.IndiceOhlcvResponse> indiceOhlcvResponses = dataService.getIndiceOhlcvs(indiceId, type, dateTimeFrom, dateTimeTo, interpolated, pageable).stream()
                .map(DataSummaryResponse.IndiceOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

}
