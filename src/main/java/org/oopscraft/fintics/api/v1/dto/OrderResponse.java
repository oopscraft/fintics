package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.OrderResult;

import java.time.LocalDateTime;

@Builder
@Getter
public class OrderResponse {

    private String orderId;

    private LocalDateTime orderAt;

    private OrderKind orderKind;

    private String tradeId;

    private String symbol;

    private String name;

    private Integer quantity;

    private OrderResult orderResult;

    private String errorMessage;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderAt(order.getOrderAt())
                .orderKind(order.getOrderKind())
                .tradeId(order.getTradeId())
                .symbol(order.getSymbol())
                .name(order.getName())
                .quantity(order.getQuantity())
                .orderResult(order.getOrderResult())
                .errorMessage(order.getErrorMessage())
                .build();
    }

}
