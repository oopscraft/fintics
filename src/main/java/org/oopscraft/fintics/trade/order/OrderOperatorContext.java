package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.OrderBook;
import org.oopscraft.fintics.model.Trade;
import org.springframework.context.ApplicationContext;

@Builder
@Getter
public class OrderOperatorContext {

    private String operatorId;

    private ApplicationContext applicationContext;

    private TradeClient tradeClient;

    private Trade trade;

    private Balance balance;

    private OrderBook orderBook;

    protected Logger log;

}
