package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.OrderBook;
import org.oopscraft.fintics.model.Trade;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;

@Builder
@Getter
public class OrderOperatorContext {

    @NotNull
    private String id;

    @NotNull
    private ApplicationContext applicationContext;

    @NotNull
    private TradeClient tradeClient;

    @NotNull
    private Trade trade;

    @NotNull
    private Balance balance;

    @NotNull
    private OrderBook orderBook;

    @NotNull
    private Logger log;

}
