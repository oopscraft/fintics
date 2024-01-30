package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fintics_broker_asset")
@IdClass(BrokerAssetEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BrokerAssetEntity extends AssetEntity {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Pk implements Serializable {
        private String brokerId;
        private String assetId;
    }

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

}
