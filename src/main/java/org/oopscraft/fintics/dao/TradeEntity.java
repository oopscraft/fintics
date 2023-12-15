package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanToYNConverter;
import org.oopscraft.fintics.model.OrderType;

import javax.persistence.*;
import java.time.LocalTime;
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
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "name")
    private String name;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private boolean enabled;

    @Column(name = "interval")
    private Integer interval;

    @Column(name = "start_at")
    private LocalTime startAt;

    @Column(name = "end_at")
    private LocalTime endAt;

    @Column(name = "client_type")
    private String clientType;

    @Column(name = "client_properties")
    @Lob
    private String clientProperties;

    @Column(name = "hold_condition")
    @Lob
    private String holdCondition;

    @Column(name = "order_type", length = 16)
    private OrderType orderType;

    @Column(name = "alarm_id", length = 32)
    private String alarmId;

    @Column(name = "alarm_on_error", length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private boolean alarmOnError;

    @Column(name = "alarm_on_order", length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private boolean alarmOnOrder;

    @Column(name = "user_id", length = 32)
    private String userId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trade_id", updatable = false)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private List<TradeAssetEntity> tradeAssetEntities = new ArrayList<>();

}
