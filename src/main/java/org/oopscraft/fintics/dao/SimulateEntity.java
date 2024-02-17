package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_simulate", indexes = {
        @Index(name = "ix_fintics_simulate_trade_id", columnList = "trade_id"),
        @Index(name = "ix_fintics_simulate_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateEntity extends BaseEntity {

    @Id
    @Column(name = "simulate_id", length = 32)
    private String simulateId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "status", length = 16)
    @Convert(converter = Simulate.StatusConverter.class)
    private Simulate.Status status;

    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "trade_data")
    @Lob
    private String tradeData;

    @Column(name = "date_time_from")
    private LocalDateTime dateTimeFrom;

    @Column(name = "date_time_to")
    private LocalDateTime dateTimeTo;

    @Column(name = "invest_amount")
    private BigDecimal investAmount;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "balance_data")
    @Lob
    private String balanceData;

    @Column(name = "orders_data")
    @Lob
    private String ordersData;

}
