package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.BrokerAssetResponse;
import org.oopscraft.fintics.api.v1.dto.BrokerResponse;
import org.oopscraft.fintics.model.BrokerAssetSearch;
import org.oopscraft.fintics.model.BrokerAsset;
import org.oopscraft.fintics.service.BrokerService;
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
@RequestMapping("/api/v1/broker")
@RequiredArgsConstructor
@Slf4j
public class BrokerRestController {

    private final static String BROKER_REST_CONTROLLER_GET_ASSET_INDICATOR = "BrokerRestController.getAssetIndicator";

    private final BrokerService brokerService;

    @GetMapping
    public ResponseEntity<List<BrokerResponse>> getBrokers() {
        List<BrokerResponse> brokerResponses = brokerService.getBrokers().stream()
                .map(BrokerResponse::from)
                .toList();
        return ResponseEntity.ok(brokerResponses);
    }

    @GetMapping("{brokerId}")
    public ResponseEntity<BrokerResponse> getBroker(@PathVariable("brokerId") String brokerId) {
        BrokerResponse brokerResponse = brokerService.getBroker(brokerId)
                .map(BrokerResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(brokerResponse);
    }

    @GetMapping("{brokerId}/asset")
    public ResponseEntity<List<BrokerAssetResponse>> getAssets(
            @PathVariable("brokerId") String brokerId,
            BrokerAssetSearch brokerAssetSearch,
            Pageable pageable
    ) {
        Page<BrokerAsset> brokerAssetPage = brokerService.getBrokerAssets(brokerId, brokerAssetSearch, pageable);
        List<BrokerAssetResponse> brokerAssetResponses = brokerAssetPage.getContent().stream()
                .map(BrokerAssetResponse::from)
                .toList();
        long total = brokerAssetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("broker-asset", pageable, total))
                .body(brokerAssetResponses);
    }

    @GetMapping("{brokerId}/asset/{assetId}")
    public ResponseEntity<BrokerAssetResponse> getAsset(
        @PathVariable("brokerId") String brokerId,
        @PathVariable("assetId") String assetId
    ){
        BrokerAssetResponse assetResponse = brokerService.getBrokerAsset(brokerId, assetId)
                .map(BrokerAssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetResponse);
    }

    @GetMapping("{brokerId}/asset/{assetId}/indicator")
    @Cacheable(cacheNames = BROKER_REST_CONTROLLER_GET_ASSET_INDICATOR, key = "#brokerId + '_' + #assetId + '_' + #dateTimeFrom + '_' + #dateTimeTo")
    public ResponseEntity<AssetIndicatorResponse> getAssetIndicator(
            @PathVariable("brokerId") String brokerId,
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
        AssetIndicatorResponse assetIndicatorResponse = brokerService.getAssetIndicator(brokerId, assetId, dateTimeFrom, dateTimeTo)
                .map(AssetIndicatorResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetIndicatorResponse);
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    @PreAuthorize("permitAll()")
    @CacheEvict(cacheNames = BROKER_REST_CONTROLLER_GET_ASSET_INDICATOR, allEntries = true)
    public void cacheEvictIndiceIndicator() {
        log.info("IndiceRestController.cacheEvictIndiceIndicator");
    }

}
