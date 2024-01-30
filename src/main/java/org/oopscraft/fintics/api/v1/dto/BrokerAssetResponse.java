package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BrokerAsset;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAssetResponse extends AssetResponse {

    private String brokerId;

    public static BrokerAssetResponse from(BrokerAsset brokerAsset) {
        return BrokerAssetResponse.builder()
                .brokerId(brokerAsset.getBrokerId())
                .assetId(brokerAsset.getAssetId())
                .assetName(brokerAsset.getAssetName())
                .links(AssetResponse.LinkResponse.from(brokerAsset.getLinks()))
                .build();
    }

}
