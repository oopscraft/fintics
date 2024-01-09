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
        private String clientId;
        private String symbol;
        private OhlcvType ohlcvType;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "client_id", length = 32)
    private String clientId;

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

}
