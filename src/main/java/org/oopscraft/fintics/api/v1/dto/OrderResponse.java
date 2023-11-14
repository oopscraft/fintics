package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class OrderResponse {

    private String orderId;

    private LocalDateTime orderAt;

    private OrderKind orderKind;

    private String tradeId;

    @Setter
    private String tradeName;

    private String symbol;

    private String assetName;

    private OrderType orderType;

    private Integer quantity;

    private BigDecimal price;

    private OrderResult orderResult;

    private String errorMessage;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderAt(order.getOrderAt())
                .orderKind(order.getOrderKind())
                .tradeId(order.getTradeId())
                .symbol(order.getSymbol())
                .assetName(order.getAssetName())
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .orderResult(order.getOrderResult())
                .errorMessage(order.getErrorMessage())
                .build();
    }

}
