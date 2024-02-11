package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Link;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private Asset.Type type;

    private String exchange;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .type(asset.getType())
                .exchange(asset.getExchange())
                .dateTime(asset.getDateTime())
                .marketCap(asset.getMarketCap())
                .issuedShares(asset.getIssuedShares())
                .per(asset.getPer())
                .roe(asset.getRoe())
                .roa(asset.getRoa())
                .links(LinkResponse.from(asset.getLinks()))
                .build();
    }

}