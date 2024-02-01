package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BrokerAsset;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAssetResponse extends AssetResponse {

    private String brokerId;

    private BrokerAsset.Type type;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    public static BrokerAssetResponse from(BrokerAsset brokerAsset) {
        return BrokerAssetResponse.builder()
                .brokerId(brokerAsset.getBrokerId())
                .assetId(brokerAsset.getAssetId())
                .assetName(brokerAsset.getAssetName())
                .type(brokerAsset.getType())
                .dateTime(brokerAsset.getDateTime())
                .marketCap(brokerAsset.getMarketCap())
                .issuedShares(brokerAsset.getIssuedShares())
                .per(brokerAsset.getPer())
                .roe(brokerAsset.getRoe())
                .roa(brokerAsset.getRoa())
                .links(AssetResponse.LinkResponse.from(brokerAsset.getLinks()))
                .build();
    }

}
