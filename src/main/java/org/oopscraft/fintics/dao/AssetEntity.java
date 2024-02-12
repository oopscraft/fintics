package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_asset")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "market", length = 16)
    private String market;

    @Column(name = "exchange", length = 16)
    private String exchange;

    @Column(name = "type", length = 16)
    private String type;

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
