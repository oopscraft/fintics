package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetOhlcvResponse;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceOhlcvResponse;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.IndiceId;
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

import java.util.List;

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
        List<AssetResponse> assetResponses = dataService.getAssets(assetId, assetName, type, pageable).stream()
                .map(AssetResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("assets", pageable))
                .body(assetResponses);
    }

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<AssetOhlcvResponse>> getAssetOhlcvs(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "interpolated", required = false) Boolean interpolated,
            Pageable pageable
    ) {
        List<AssetOhlcvResponse> assetOhlcvResponses = dataService.getAssetOhlcvs(assetId, type, interpolated, pageable).stream()
                .map(AssetOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-ohlcvs", pageable))
                .body(assetOhlcvResponses);
    }

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<IndiceOhlcvResponse>> getIndiceOhlcvs(
            @RequestParam(value = "indiceId", required = false) IndiceId indiceId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "interpolated", required = false) Boolean interpolated,
            Pageable pageable
    ) {
        List<IndiceOhlcvResponse> indiceOhlcvResponses = dataService.getIndiceOhlcvs(indiceId, type, interpolated, pageable).stream()
                .map(IndiceOhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

}
