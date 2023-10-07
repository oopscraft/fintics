package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;
import org.oopscraft.arch4j.core.role.dao.RoleEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fintics_trade_asset")
@IdClass(TradeAssetEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetEntity extends SystemFieldEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private String symbol;
    }

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Id
    @Column(name = "symbol")
    private String symbol;

    @Column(name = "enabled")
    private boolean enabled;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "symbol",
            referencedColumnName = "symbol",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private AssetEntity assetEntity;

}
