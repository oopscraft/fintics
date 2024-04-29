package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    public List<Link> getLinks() {
        return Optional.ofNullable(getAssetId())
                .map(value -> value.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> {
                    String market = array[0];
                    String symbol = array[1];
                    List<Link> links = new ArrayList<>();
                    switch (market) {
                        case "US" ->
                                links.add(Link.of("Yahoo", "https://finance.yahoo.com/quote/" + symbol));
                        case "KR" ->
                                links.add(Link.of("Naver", "https://finance.naver.com/item/main.naver?code=" + symbol));
                        case "UPBIT" ->
                                links.add(Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + symbol));
                    }
                    return links;
                })
                .orElse(new ArrayList<>());
    }

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .assetName(assetEntity.getAssetName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .type(assetEntity.getType())
                .dateTime(assetEntity.getDateTime())
                .marketCap(assetEntity.getMarketCap())
                .issuedShares(assetEntity.getIssuedShares())
                .per(assetEntity.getPer())
                .roe(assetEntity.getRoe())
                .roa(assetEntity.getRoa())
                .build();
    }

}
