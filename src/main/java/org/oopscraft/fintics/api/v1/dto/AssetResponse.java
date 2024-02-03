package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetResponse {

    private String assetId;

    private String assetName;

    private String exchangeId;

    private Asset.Type type;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    public static AssetResponse from(Asset brokerAsset) {
        return AssetResponse.builder()
                .assetId(brokerAsset.getAssetId())
                .assetName(brokerAsset.getAssetName())
                .type(brokerAsset.getType())
                .dateTime(brokerAsset.getDateTime())
                .marketCap(brokerAsset.getMarketCap())
                .issuedShares(brokerAsset.getIssuedShares())
                .per(brokerAsset.getPer())
                .roe(brokerAsset.getRoe())
                .roa(brokerAsset.getRoa())
                .links(LinkResponse.from(brokerAsset.getLinks()))
                .build();
    }

}