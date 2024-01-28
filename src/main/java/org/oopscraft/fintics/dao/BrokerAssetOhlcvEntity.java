package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Ohlcv;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_broker_asset_ohlcv")
@IdClass(BrokerAssetOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAssetOhlcvEntity extends OhlcvEntity {

    public static class Pk implements Serializable {
        private String brokerId;
        private String assetId;
        private Ohlcv.Type type;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

}
