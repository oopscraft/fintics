package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.OhlcvType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_asset_ohlcv")
@IdClass(AssetOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvEntity extends OhlcvEntity {

    public static class Pk implements Serializable {
        private String tradeClientId;
        private String id;
        private OhlcvType ohlcvType;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "trade_client_id", length = 32)
    private String tradeClientId;

    @Id
    @Column(name = "id", length = 32)
    private String id;

}
