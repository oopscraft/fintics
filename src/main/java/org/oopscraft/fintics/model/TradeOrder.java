package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.TradeOrderEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.LocalDateTime;

@Builder
@Getter
public class TradeOrder {

    private String tradeId;

    private LocalDateTime orderAt;

    private OrderType orderType;

    private String symbol;

    private String name;

    private Integer quantity;

    private OrderResult orderResult;

    private String errorMessage;

    public static TradeOrder from(TradeOrderEntity tradeOrderEntity) {
        return TradeOrder.builder()
                .tradeId(tradeOrderEntity.getTradeId())
                .orderAt(tradeOrderEntity.getOrderAt())
                .orderType(tradeOrderEntity.getOrderType())
                .symbol(tradeOrderEntity.getSymbol())
                .name(tradeOrderEntity.getName())
                .quantity(tradeOrderEntity.getQuantity())
                .orderResult(tradeOrderEntity.getOrderResult())
                .errorMessage(tradeOrderEntity.getErrorMessage())
                .build();
    }

}
