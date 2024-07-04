package org.oopscraft.fintics.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    private Financial assetFinancial;

    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

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
        // financial
        Financial assetFinancial = Optional.ofNullable(assetEntity.getFinancialEntity())
                .map(Financial::from)
                .orElse(Financial.builder()
                        .assetId(asset.getAssetId())
                        .build());
        asset.setAssetFinancial(assetFinancial);
        // return
        return asset;
    }

}
