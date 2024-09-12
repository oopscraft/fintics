package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.common.data.PageableAsQueryParam;
import org.oopscraft.arch4j.web.common.data.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetSearch;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('API_ASSETS')")
@Tag(name = "assets", description = "Assets")
@RequiredArgsConstructor
@Slf4j
public class AssetsRestController {

    private final AssetService assetService;

    /**
     * gets list of assets
     * @param assetId asset id
     * @param name asset name
     * @param market market
     * @param favorite favorite
     * @param perFrom PER from
     * @param perTo PER to
     * @param roeFrom ROE from
     * @param roeTo ROE to
     * @param roaFrom ROA from
     * @param roaTo ROA to
     * @param pageable pageable
     * @return list of assets
     */
    @GetMapping
    @Operation(summary = "gets list of assets")
    @PageableAsQueryParam
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false)
            @Parameter(name ="asset id", description = "asset id", example="US.AAPL")
                    String assetId,
            @RequestParam(value = "name", required = false)
            @Parameter(name = "name", description = "asset name")
                    String name,
            @RequestParam(value = "market", required = false)
            @Parameter(name= "market", description = "US|KR|...")
                    String market,
            @RequestParam(value = "type", required = false)
            @Parameter(name = "type", description = "STOCK|ETF")
                    String type,
            @RequestParam(value = "favorite", required = false)
            @Parameter(name = "favorite", description = "favorite")
                    Boolean favorite,
            @RequestParam(value = "perFrom", required = false)
            @Parameter(name = "perFrom", description = "range of Price to Earnings Ratio")
                    BigDecimal perFrom,
            @RequestParam(value = "perTo", required = false)
            @Parameter(name = "perTo", description = "range of Price to Earnings Ratio")
                    BigDecimal perTo,
            @RequestParam(value = "roeFrom", required = false)
            @Parameter(name = "roeFrom", description = "range of Return On Equity")
                    BigDecimal roeFrom,
            @RequestParam(value = "roeTo", required = false)
            @Parameter(name= "roaTo", description = "range of Return On Equity")
                    BigDecimal roeTo,
            @RequestParam(value = "roaFrom", required = false)
            @Parameter(name = "roaFrom", description = "range of Return On Assets")
                    BigDecimal roaFrom,
            @RequestParam(value = "roaTo", required = false)
            @Parameter(name = "roaTo", description = "range of Return On Assets")
                    BigDecimal roaTo,
            @Parameter(hidden = true)
                    Pageable pageable
    ) {
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .name(name)
                .market(market)
                .type(type)
                .favorite(favorite)
                .perFrom(perFrom)
                .perTo(perTo)
                .roeFrom(roeFrom)
                .roeTo(roeTo)
                .roaFrom(roaFrom)
                .roaTo(roaTo)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .toList();
        long total = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(assetResponses);
    }

    /**
     * gets specific asset
     * @param assetId asset id
     * @return asset response
     */
    @GetMapping("{assetId}")
    @Operation(description = "get asset info")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable("assetId")
            @Parameter(name = "asset id", description = "asset id", example = "US.AAPL")
                    String assetId
    ){
        AssetResponse assetResponse = assetService.getAsset(assetId)
                .map(AssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetResponse);
    }

}
