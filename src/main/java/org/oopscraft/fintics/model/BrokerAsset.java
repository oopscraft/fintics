package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.BrokerAssetEntity;
import org.oopscraft.fintics.dao.TradeAssetEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAsset extends Asset {

    private String brokerId;

    public static BrokerAsset from(BrokerAssetEntity brokerAssetEntity) {
        BrokerAsset brokerAsset = BrokerAsset.builder()
                .brokerId(brokerAssetEntity.getBrokerId())
                .assetId(brokerAssetEntity.getAssetId())
                .assetName(brokerAssetEntity.getAssetName())
                .build();
        BrokerClientFactory.getBrokerClientDefinition(brokerAsset.getBrokerId()).ifPresent(brokerClientDefinition ->
                brokerAsset.setLinks(brokerClientDefinition.getAssetLinks(brokerAsset))
        );
        return brokerAsset;
    }

}
