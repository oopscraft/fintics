package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetResponse {

    private String assetId;

    private String assetName;

    private String market;

    private String exchange;

    private String type;

    private BigDecimal marketCap;

    private boolean favorite;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    private FinancialResponse assetFinancial;

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .type(asset.getType())
                .marketCap(asset.getMarketCap())
                .favorite(asset.isFavorite())
                .links(LinkResponse.from(asset.getLinks()))
                .assetFinancial(Optional.ofNullable(asset.getAssetFinancial())
                        .map(FinancialResponse::from)
                        .orElse(null))
                .build();
    }

}