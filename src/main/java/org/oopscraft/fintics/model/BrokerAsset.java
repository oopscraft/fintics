package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
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
        return BrokerAsset.builder()
                .brokerId(brokerAssetEntity.getBrokerId())
                .assetId(brokerAssetEntity.getAssetId())
                .assetName(brokerAssetEntity.getAssetName())
                .build();
    }

}
