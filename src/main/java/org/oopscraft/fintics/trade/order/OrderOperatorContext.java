package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.OrderBook;
import org.oopscraft.fintics.model.Trade;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.validation.constraints.NotNull;

@Builder
@Getter
public class OrderOperatorContext {

    private final String id;

    private final TradeClient tradeClient;

    private final Trade trade;

    private final Balance balance;

    private final OrderBook orderBook;

    private final PlatformTransactionManager transactionManager;

}
