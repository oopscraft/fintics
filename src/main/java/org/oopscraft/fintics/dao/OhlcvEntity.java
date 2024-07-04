package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.AssetOhlcv;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fintics_ohlcv")
@IdClass(AssetOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private AssetOhlcv.Type type;
        private Instant datetime;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "type", length = 32)
    @Enumerated(EnumType.STRING)
    private AssetOhlcv.Type type;

    @Id
    @Column(name = "datetime")
    private Instant datetime;

    @Column(name = "open", scale = 4)
    private BigDecimal open;

    @Column(name = "high", scale = 4)
    private BigDecimal high;

    @Column(name = "low", scale = 4)
    private BigDecimal low;

    @Column(name = "close", scale = 4)
    private BigDecimal close;

    @Column(name = "volume")
    private BigDecimal volume;

}
