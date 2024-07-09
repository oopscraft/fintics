package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.BasketRequest;
import org.oopscraft.fintics.api.v1.dto.BasketResponse;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.BasketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/baskets")
@PreAuthorize("hasAuthority('API_BASKETS')")
@Tag(name = "baskets", description = "Baskets")
@RequiredArgsConstructor
public class BasketsRestController {

    private final BasketService basketService;

    @GetMapping
    @Operation(summary = "get list of basket")
    public ResponseEntity<List<BasketResponse>> getBrokers(
            @RequestParam(value = "basketName", required = false)
            @Parameter(description = "basket name")
                    String basketName,
            @PageableDefault
            @Parameter(hidden = true)
                    Pageable pageable
    ) {
        BasketSearch basketSearch = BasketSearch.builder()
                .basketName(basketName)
                .build();
        Page<Basket> basketPage = basketService.getBaskets(basketSearch, pageable);
        List<BasketResponse> basketResponses = basketPage.getContent().stream()
                .map(BasketResponse::from)
                .toList();
        long total = basketPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("basket", pageable, total))
                .body(basketResponses);
    }

    @GetMapping("{basketId}")
    @Operation(summary = "gets basket info")
    public ResponseEntity<BasketResponse> getBasket(
            @PathVariable("basketId")
            @Parameter(description = "basket id")
                    String basketId
    ) {
        BasketResponse basketResponse = basketService.getBasket(basketId)
                .map(BasketResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(basketResponse);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('API_BASKETS_EDIT')")
    @Operation(summary = "creates new basket")
    public ResponseEntity<BasketResponse> createBasket(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "basket request payload")
                    BasketRequest basketRequest
    ) {
        // basket
        Basket basket = Basket.builder()
                .basketName(basketRequest.getBasketName())
                .build();
        // basket assets
        List<BasketAsset> basketAssets = basketRequest.getBasketAssets().stream()
                .map(basketAssetRequest ->
                        BasketAsset.builder()
                                .assetId(basketAssetRequest.getAssetId())
                                .fixed(basketAssetRequest.isFixed())
                                .enabled(basketAssetRequest.isEnabled())
                                .holdingWeight(basketAssetRequest.getHoldingWeight())
                        .build())
                .collect(Collectors.toList());
        basket.setBasketAssets(basketAssets);
        // save
        Basket savedBasket = basketService.saveBasket(basket);
        // response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasketResponse.from(savedBasket));
    }

    @PutMapping("{basketId}")
    @PreAuthorize("hasAuthority('API_BASKETS_EDIT')")
    @Operation(summary = "modifies specified broker info")
    public ResponseEntity<BasketResponse> modifyBasket(
            @PathVariable("basketId")
            @Parameter(description = "basket id")
                    String basketId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "broker request payload")
                    BasketRequest basketRequest
    ) {
        // basket
        Basket basket = basketService.getBasket(basketId).orElseThrow();
        basket.setBasketName(basketRequest.getBasketName());
        // basket assets
        List<BasketAsset> basketAssets = basketRequest.getBasketAssets().stream()
                .map(basketAssetRequest -> {
                    return BasketAsset.builder()
                            .assetId(basketAssetRequest.getAssetId())
                            .fixed(basketAssetRequest.isFixed())
                            .enabled(basketAssetRequest.isEnabled())
                            .holdingWeight(basketAssetRequest.getHoldingWeight())
                            .build();
                })
                .collect(Collectors.toList());
        basket.setBasketAssets(basketAssets);
        // save
        Basket savedBasket = basketService.saveBasket(basket);
        return ResponseEntity.ok(BasketResponse.from(savedBasket));
    }

    @DeleteMapping("{basketId}")
    @PreAuthorize("hasAuthority('API_BASKETS_EDIT')")
    @Operation(summary = "deletes specified basket")
    public ResponseEntity<Void> deleteBasket(
            @PathVariable("basketId")
            @Parameter(name = "basket id", description = "basket id")
                    String basketId
    ) {
        basketService.deleteBasket(basketId);
        return ResponseEntity.ok().build();
    }

}
