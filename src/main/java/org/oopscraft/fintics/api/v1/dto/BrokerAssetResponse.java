package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BrokerAsset;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAssetResponse {

    private String brokerId;

    private String assetId;

    private String assetName;

    public static BrokerAssetResponse from(BrokerAsset brokerAsset) {
        return BrokerAssetResponse.builder()
                .brokerId(brokerAsset.getBrokerId())
                .assetId(brokerAsset.getAssetId())
                .assetName(brokerAsset.getAssetName())
                .build();
    }

}
