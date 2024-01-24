package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_simulate")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateEntity extends BaseEntity {

    @Id
    @Column(name = "simulate_id", length = 32)
    private String simulateId;

    @Column(name = "hold_condition")
    @Lob
    private String holdCondition;

    @Column(name = "date_time_from")
    private LocalDateTime dateTimeFrom;

    @Column(name = "date_time_to")
    private LocalDateTime dateTimeTo;

    @Column(name = "invest_amount")
    private BigDecimal investAmount;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "result")
    private String result;

    @Column(name = "result_message")
    @Lob
    private String resultMessage;

}
