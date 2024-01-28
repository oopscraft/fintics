package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.Order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fintics_order",
        indexes = {
                @Index(name = "ix_fintics_order_trade_id", columnList = "trade_id")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderEntity extends BaseEntity {

    @Id
    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "order_at")
    private LocalDateTime orderAt;

    @Column(name = "type", length = 8)
    private Order.Type type;

    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "kind", length = 16)
    private Order.Kind kind;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "result")
    private Order.Result result;

    @Column(name = "error_message")
    @Lob
    private String errorMessage;

}
