package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class OrderResponse {

    private String orderId;

    private LocalDateTime orderAt;

    private Order.Type type;

    private String tradeId;

    @Setter
    private String tradeName;

    private String assetId;

    private String assetName;

    private Order.Kind kind;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal purchasePrice;

    private BigDecimal realizedProfitAmount;

    private Order.Result result;

    private String errorMessage;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderAt(order.getOrderAt())
                .type(order.getType())
                .tradeId(order.getTradeId())
                .assetId(order.getAssetId())
                .assetName(order.getAssetName())
                .kind(order.getKind())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .purchasePrice(order.getPurchasePrice())
                .realizedProfitAmount(order.getRealizedProfitAmount())
                .result(order.getResult())
                .errorMessage(order.getErrorMessage())
                .build();
    }

}
