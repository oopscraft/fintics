package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.OhlcvResponse;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('API_ASSETS')")
@Tag(name = "assets", description = "Assets")
@RequiredArgsConstructor
@Slf4j
public class AssetsRestController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "market", required = false) String market,
            Pageable pageable
    ) {
        Page<Asset> assetPage = assetService.getAssets(assetId, assetName, market, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .toList();
        long total = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(assetResponses);
    }

    @GetMapping("{assetId}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable("assetId") String assetId){
        AssetResponse assetResponse = assetService.getAsset(assetId)
                .map(AssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetResponse);
    }

    @GetMapping("{assetId}/ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getAssetOhlcvs(
            @PathVariable("assetId") String assetId,
            @RequestParam("type") Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(1000,1,1,0,0));
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(9999,12,31,23,59,59));
        List<OhlcvResponse> assetOhlcvResponses = assetService.getAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(assetOhlcvResponses);
    }

}
