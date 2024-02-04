package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetOhlcvResponse;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceOhlcvResponse;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.DataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
public class DataRestController {

    private final DataService dataService;

    @GetMapping("assets")
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "type", required = false) Asset.Type type,
            Pageable pageable
    ) {
        Page<Asset> assetPage = dataService.getAssets(assetId, assetName, type, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .toList();
        long total = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(assetResponses);
    }

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<AssetOhlcvResponse>> getAssetOhlcvs(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            Pageable pageable
    ) {
        Page<AssetOhlcv> assetOhlcvPage = dataService.getAssetOhlcvs(assetId, type, pageable);
        List<AssetOhlcvResponse> assetOhlcvResponses = assetOhlcvPage.getContent().stream()
                .map(AssetOhlcvResponse::from)
                .toList();
        long total = assetOhlcvPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-ohlcv", pageable, total))
                .body(assetOhlcvResponses);
    }

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<IndiceOhlcvResponse>> getIndiceOhlcvs(
            @RequestParam(value = "indiceId", required = false) IndiceId indiceId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            Pageable pageable
    ) {
        Page<IndiceOhlcv> indiceOhlcvPage = dataService.getIndiceOhlcvs(indiceId, type, pageable);
        List<IndiceOhlcvResponse> indiceOhlcvResponses = indiceOhlcvPage.getContent().stream()
                .map(IndiceOhlcvResponse::from)
                .toList();
        long total = indiceOhlcvPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-ohlcv", pageable, total))
                .body(indiceOhlcvResponses);
    }

}
