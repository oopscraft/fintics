package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BrokerAsset;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(name = "type", length = 16)
    private BrokerAsset.Type type;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "market_cap", precision = 32)
    private BigDecimal marketCap;

    @Column(name = "issued_shares", precision = 32)
    private BigDecimal issuedShares;

    @Column(name = "per", scale = 2)
    private BigDecimal per;

    @Column(name = "roe", scale = 2)
    private BigDecimal roe;

    @Column(name = "roa", scale = 2)
    private BigDecimal roa;

}
