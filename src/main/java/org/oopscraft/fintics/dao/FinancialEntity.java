package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fintics_financial")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FinancialEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "date_time")
    private Instant dateTime;

    @Column(name = "issued_shares", precision = 32)
    private BigDecimal issuedShares;

    @Column(name = "total_assets")
    private BigDecimal totalAssets;

    @Column(name = "total_equity")
    private BigDecimal totalEquity;

    @Column(name = "net_income")
    private BigDecimal netIncome;

    @Column(name = "eps", scale = 2)
    private BigDecimal eps;

    @Column(name = "per", scale = 2)
    private BigDecimal per;

    @Column(name = "roe", scale = 2)
    private BigDecimal roe;

    @Column(name = "roa", scale = 2)
    private BigDecimal roa;

    @Column(name = "ebitda")
    private BigDecimal ebitda;

    @Column(name = "dividend_yield", scale = 2)
    private BigDecimal dividendYield;

}
