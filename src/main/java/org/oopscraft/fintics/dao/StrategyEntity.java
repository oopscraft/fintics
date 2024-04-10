package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.Strategy;

import javax.persistence.*;

@Entity
@Table(name = "fintics_strategy")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyEntity extends BaseEntity {

    @Id
    @Column(name = "strategy_id", length = 32)
    private String strategyId;

    @Column(name = "strategy_name")
    private String strategyName;

    @Column(name = "language", length = 16)
    private Strategy.Language language;

    @Column(name = "variables")
    @Lob
    private String variables;

    @Column(name = "script")
    @Lob
    private String script;

}
