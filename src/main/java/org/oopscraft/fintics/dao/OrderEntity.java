package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.OrderType;
import org.oopscraft.fintics.model.OrderResult;

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
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_at")
    private LocalDateTime orderAt;

    @Column(name = "order_type", length = 8)
    private OrderType orderType;

    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "order_kind", length = 16)
    private OrderKind orderKind;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "order_result")
    private OrderResult orderResult;

    @Column(name = "error_message")
    @Lob
    private String errorMessage;

}
