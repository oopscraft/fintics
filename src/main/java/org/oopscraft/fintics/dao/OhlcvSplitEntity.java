package org.oopscraft.fintics.dao;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fintics_ohlcv_split")
@IdClass(AssetOhlcvSplitEntity.Pk.class)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class AssetOhlcvSplitEntity {

    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private String assetId;
        private Instant datetime;
    }

    @Id
    @Column(name = "asset_id")
    private String assetId;

    @Id
    @Column(name = "datetime")
    private Instant datetime;

    @Column(name = "split_from")
    private BigDecimal splitFrom;

    @Column(name = "split_to")
    private BigDecimal splitTo;

}
