package org.oopscraft.fintics.dao;

import lombok.*;
import org.oopscraft.fintics.model.AssetType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "fintics_asset")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetEntity {

    @Id
    @Column(name = "symbol")
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private AssetType type;

    @Column(name = "country")
    private String country;

    @Column(name = "currency")
    private String currency;

    @Column(name = "eps")
    private BigDecimal eps;

    @Column(name = "roe")
    private BigDecimal roe;

    @Column(name = "roa")
    private BigDecimal roa;

    @Column(name = "dividendYield")
    private BigDecimal dividendYield;

}
