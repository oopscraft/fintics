package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanConverter;
import org.oopscraft.fintics.model.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
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
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

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

    @Column(name = "strategy_data")
    @Lob
    private String strategyData;

    @Column(name = "invest_from")
    private LocalDateTime investFrom;

    @Column(name = "invest_to")
    private LocalDateTime investTo;

    @Column(name = "invest_amount")
    private BigDecimal investAmount;

    @Column(name = "fee_rate", scale = 4)
    private BigDecimal feeRate;

    @Column(name = "favorite")
    @Convert(converter = BooleanConverter.class)
    private boolean favorite;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "profit_amount")
    private BigDecimal profitAmount;

    @Column(name = "profit_percentage")
    private BigDecimal profitPercentage;

    @Column(name = "balance_data")
    @Lob
    private String balanceData;

    @Column(name = "orders_data")
    @Lob
    private String ordersData;

    @Column(name = "simulate_report_data")
    @Lob
    private String simulateReportData;

}
