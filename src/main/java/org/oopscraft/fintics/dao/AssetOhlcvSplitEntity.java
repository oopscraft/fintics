package org.oopscraft.fintics.dao;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_asset_ohlcv_split")
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
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "asset_id")
    private String assetId;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "split_from")
    private BigDecimal splitFrom;

    @Column(name = "split_to")
    private BigDecimal splitTo;

}
