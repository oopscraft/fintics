package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.code.dao.CodeItemEntity_;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fintics_trade")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeEntity extends SystemFieldEntity {

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "name")
    private String name;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "interval")
    private Integer interval;

    @Column(name = "client_properties")
    @Lob
    private String clientProperties;

    @Column(name = "buy_rule")
    @Lob
    private String buyRule;

    @Column(name = "sell_rule")
    @Lob
    private String sellRule;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trade_id", updatable = false)
    @Builder.Default
    private List<TradeAssetEntity> tradeAssetEntities = new ArrayList<>();

}
