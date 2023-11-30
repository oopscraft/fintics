package org.oopscraft.fintics.simulate;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Trade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class Simulate {

    private Trade trade;

    private Balance balance;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Double feeRate;

    private Double bidAskSpread;

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        this.orders.add(order);
    }

}
