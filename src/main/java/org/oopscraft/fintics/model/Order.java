package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.OrderEntity;

import java.time.LocalDateTime;

@Builder
@Getter
public class Order {

    private String orderId;

    private LocalDateTime orderAt;

    private OrderKind orderKind;

    private String tradeId;

    private String symbol;

    private String name;

    private Integer quantity;

    private OrderResult orderResult;

    private String errorMessage;

    public static Order from(OrderEntity OrderEntity) {
        return Order.builder()
                .orderId(OrderEntity.getOrderId())
                .orderAt(OrderEntity.getOrderAt())
                .orderKind(OrderEntity.getOrderKind())
                .tradeId(OrderEntity.getTradeId())
                .symbol(OrderEntity.getSymbol())
                .name(OrderEntity.getName())
                .quantity(OrderEntity.getQuantity())
                .orderResult(OrderEntity.getOrderResult())
                .errorMessage(OrderEntity.getErrorMessage())
                .build();
    }

}
