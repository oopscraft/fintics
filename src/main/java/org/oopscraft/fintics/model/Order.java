package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.oopscraft.fintics.dao.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
public class Order {

    private String orderId;

    private LocalDateTime orderAt;

    private OrderType orderType;

    private String tradeId;

    private String assetId;

    private String assetName;

    private OrderKind orderKind;

    private BigDecimal quantity;

    private BigDecimal price;

    private String clientOrderId;

    private OrderResult orderResult;

    private String errorMessage;

    public static Order from(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(orderEntity.getOrderId())
                .orderAt(orderEntity.getOrderAt())
                .orderType(orderEntity.getOrderType())
                .tradeId(orderEntity.getTradeId())
                .assetId(orderEntity.getAssetId())
                .assetName(orderEntity.getAssetName())
                .orderKind(orderEntity.getOrderKind())
                .quantity(orderEntity.getQuantity())
                .price(orderEntity.getPrice())
                .orderResult(orderEntity.getOrderResult())
                .errorMessage(orderEntity.getErrorMessage())
                .build();
    }

}
