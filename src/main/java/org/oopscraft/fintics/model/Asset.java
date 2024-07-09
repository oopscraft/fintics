package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset implements Serializable {

    private String assetId;

    private String assetName;

    private String market;

    private String exchange;

    private String type;

    private BigDecimal marketCap;

    private boolean favorite;

    @Builder.Default
    private List<AssetMeta> assetMetas = new ArrayList<>();

    /**
     * gets symbol
     * @return symbol
     */
    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    /**
     * get asset icon
     * @return icon url
     */
    public String getIcon() {
        return IconFactory.getIcon(this);
    }

    /**
     * gets asset link
     * @return link url
     */
    public List<Link> getLinks() {
        return LinkFactory.getLinks(this);
    }

    public static Asset from(AssetEntity assetEntity) {
        Asset asset = Asset.builder()
                .assetId(assetEntity.getAssetId())
                .assetName(assetEntity.getAssetName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .type(assetEntity.getType())
                .marketCap(assetEntity.getMarketCap())
                .favorite(assetEntity.isFavorite())
                .build();
        // asset metas
        List<AssetMeta> assetMetas = assetEntity.getAssetMetaEntities().stream()
                .map(AssetMeta::from)
                .toList();
        asset.setAssetMetas(assetMetas);
        // return
        return asset;
    }

}
