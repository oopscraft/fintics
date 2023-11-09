package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;
import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_trade_order")
@IdClass(TradeOrderEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeOrderEntity extends SystemFieldEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private LocalDateTime orderAt;
    }

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Id
    @Column(name = "order_at")
    private LocalDateTime orderAt;

    @Column(name = "order_type", length = 32)
    private OrderType orderType;

    @Column(name = "symbol", length = 32)
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "order_result")
    private OrderResult orderResult;

    @Column(name = "error_message")
    @Lob
    private String errorMessage;

}
