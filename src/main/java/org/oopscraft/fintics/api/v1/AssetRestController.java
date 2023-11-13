package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetSearch;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/asset")
@RequiredArgsConstructor
public class AssetRestController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<Asset> assetPage = assetService.getAssets(assetSearch, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .collect(Collectors.toList());
        long total = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(assetResponses);
    }

    @GetMapping("{symbol}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable("symbol") String symbol) {
        AssetResponse assetResponse = assetService.getAsset(symbol)
                .map(AssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok()
                .body(assetResponse);
    }

}
