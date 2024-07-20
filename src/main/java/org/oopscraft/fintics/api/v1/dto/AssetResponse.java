package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;

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

    private String icon;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    @Builder.Default
    private List<AssetMetaResponse> assetMetas = new ArrayList<>();

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .type(asset.getType())
                .marketCap(asset.getMarketCap())
                .favorite(asset.isFavorite())
                .icon(asset.getIcon())
                .links(LinkResponse.from(asset.getLinks()))
                .assetMetas(AssetMetaResponse.from(asset.getAssetMetas()))
                .build();
    }

}