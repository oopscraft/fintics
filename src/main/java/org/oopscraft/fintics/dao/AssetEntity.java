package org.oopscraft.fintics.dao;

import lombok.*;

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
    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

}
