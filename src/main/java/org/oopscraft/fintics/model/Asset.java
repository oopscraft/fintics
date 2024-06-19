package org.oopscraft.fintics.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String assetId;

    private String assetName;

    private String market;

    private String exchange;

    private String type;

    private boolean favorite;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal totalAssets;

    private BigDecimal totalEquity;

    private BigDecimal netIncome;

    private BigDecimal eps;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal dividendYield;

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
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .assetName(assetEntity.getAssetName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .type(assetEntity.getType())
                .favorite(assetEntity.isFavorite())
                .dateTime(assetEntity.getDateTime())
                .marketCap(assetEntity.getMarketCap())
                .issuedShares(assetEntity.getIssuedShares())
                .per(assetEntity.getPer())
                .roe(assetEntity.getRoe())
                .roa(assetEntity.getRoa())
                .build();
    }

}
