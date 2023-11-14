package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class Order {

    private String orderId;

    private LocalDateTime orderAt;

    private OrderKind orderKind;

    private String tradeId;

    private String symbol;

    private String assetName;

    private OrderType orderType;

    private Integer quantity;

    private BigDecimal price;

    private OrderResult orderResult;

    private String errorMessage;

    public static Order from(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(orderEntity.getOrderId())
                .orderAt(orderEntity.getOrderAt())
                .orderKind(orderEntity.getOrderKind())
                .tradeId(orderEntity.getTradeId())
                .symbol(orderEntity.getSymbol())
                .assetName(orderEntity.getAssetName())
                .orderType(orderEntity.getOrderType())
                .quantity(orderEntity.getQuantity())
                .price(orderEntity.getPrice())
                .orderResult(orderEntity.getOrderResult())
                .errorMessage(orderEntity.getErrorMessage())
                .build();
    }

}
