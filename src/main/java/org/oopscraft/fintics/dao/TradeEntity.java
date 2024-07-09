package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanConverter;
import org.oopscraft.arch4j.core.data.converter.ZoneIdConverter;
import org.oopscraft.fintics.model.Order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fintics_trade")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeEntity extends BaseEntity {

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean enabled;

    @Column(name = "interval")
    private Integer interval;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "start_at")
    private LocalTime startAt;

    @Column(name = "end_at")
    private LocalTime endAt;

    @Column(name = "invest_amount")
    private BigDecimal investAmount;

    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Column(name = "basket_id", length = 32)
    private String basketId;

    @Column(name = "strategy_id", length = 32)
    private String strategyId;

    @Column(name = "strategy_variables")
    @Lob
    private String strategyVariables;

    @Column(name = "order_kind", length = 16)
    private Order.Kind orderKind;

    @Column(name = "alarm_id", length = 32)
    private String alarmId;

    @Column(name = "alarm_on_error", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean alarmOnError;

    @Column(name = "alarm_on_order", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean alarmOnOrder;

}
