package org.oopscraft.fintics.trade.order;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.OrderBook;
import org.oopscraft.fintics.model.Trade;
import org.springframework.transaction.PlatformTransactionManager;

@Builder
@Getter
public class OrderOperatorContext {

    private final String id;

    private final BrokerClient tradeClient;

    private final Trade trade;

    private final Balance balance;

    private final OrderBook orderBook;

    private final PlatformTransactionManager transactionManager;

}
