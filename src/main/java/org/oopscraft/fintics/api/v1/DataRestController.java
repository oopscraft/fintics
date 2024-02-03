package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.api.v1.dto.AssetOhlcvResponse;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceOhlcvResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/data")
@PreAuthorize("hasAuthority('API_DATA')")
@RequiredArgsConstructor
@Slf4j
public class DataRestController {

    @GetMapping("assets")
    public ResponseEntity<List<AssetResponse>> getAssets() {
        List<AssetResponse> assetResponses = new ArrayList<>();
        return ResponseEntity.ok(assetResponses);
    }

    @GetMapping("asset-ohlcvs")
    public ResponseEntity<List<AssetOhlcvResponse>> getAssetOhlcvs() {
        List<AssetOhlcvResponse> assetOhlcvResponses = new ArrayList<>();
        return ResponseEntity.ok(assetOhlcvResponses);
    }

    @GetMapping("indice-ohlcvs")
    public ResponseEntity<List<IndiceOhlcvResponse>> getIndiceOhlcvs() {
        List<IndiceOhlcvResponse> indiceOhlcvResponses = new ArrayList<>();
        return ResponseEntity.ok(indiceOhlcvResponses);
    }

}
