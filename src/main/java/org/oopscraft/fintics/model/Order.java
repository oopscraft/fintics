package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.OrderEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    private String orderId;

    private LocalDateTime orderAt;

    private Type type;

    private String tradeId;

    private String assetId;

    private String assetName;

    private Kind kind;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal purchasePrice;

    private String brokerOrderId;

    private Result result;

    private String errorMessage;

    public String getSymbol() {
        return Optional.ofNullable(assetId)
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]",assetId)));
    }

    public enum Type { BUY, SELL }

    @Converter(autoApply = true)
    public static class TypeConverter extends AbstractEnumConverter<Type> {}

    public enum Kind { LIMIT, MARKET }

    @Converter(autoApply = true)
    public static class KindConverter extends AbstractEnumConverter<Kind> {}

    public enum Result { COMPLETED, FAILED }

    @Converter(autoApply = true)
    public static class ResultConverter extends AbstractEnumConverter<Result> {}

    public static Order from(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(orderEntity.getOrderId())
                .orderAt(orderEntity.getOrderAt())
                .type(orderEntity.getType())
                .tradeId(orderEntity.getTradeId())
                .assetId(orderEntity.getAssetId())
                .assetName(orderEntity.getAssetName())
                .kind(orderEntity.getKind())
                .quantity(orderEntity.getQuantity())
                .price(orderEntity.getPrice())
                .purchasePrice(orderEntity.getPurchasePrice())
                .brokerOrderId(orderEntity.getBrokerOrderId())
                .result(orderEntity.getResult())
                .errorMessage(orderEntity.getErrorMessage())
                .build();
    }

}
