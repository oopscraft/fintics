package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetSearch;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('API_ASSETS')")
@RequiredArgsConstructor
@Slf4j
public class AssetsRestController {

    private final static String ASSET_REST_CONTROLLER_GET_ASSET_INDICATOR = "AssetRestController.getAssetIndicator";

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<Asset> assetPage = assetService.getAssets(assetSearch, pageable);
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

    @GetMapping("{assetId}/indicator")
    @Cacheable(cacheNames = ASSET_REST_CONTROLLER_GET_ASSET_INDICATOR, key = "#assetId + '_' + #dateTimeFrom + '_' + #dateTimeTo")
    public ResponseEntity<AssetIndicatorResponse> getAssetIndicator(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        AssetIndicatorResponse assetIndicatorResponse = assetService.getAssetIndicator(assetId, dateTimeFrom, dateTimeTo)
                .map(AssetIndicatorResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetIndicatorResponse);
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    @PreAuthorize("permitAll()")
    @CacheEvict(cacheNames = ASSET_REST_CONTROLLER_GET_ASSET_INDICATOR, allEntries = true)
    public void cacheEvictAssetIndicator() {
        log.info("cacheEvictAssetIndicator");
    }

}
