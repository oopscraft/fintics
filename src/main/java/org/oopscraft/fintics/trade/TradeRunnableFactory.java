package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.BrokerService;
import org.oopscraft.fintics.service.StrategyService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeRunnableFactory {

    private final TradeService tradeService;

    private final StrategyService strategyService;

    private final BrokerService brokerService;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final IndiceClient indiceClient;

    private final BrokerClientFactory brokerClientFactory;

    private final StatusHandlerFactory statusHandlerFactory;

    private final PlatformTransactionManager transactionManager;

    public TradeRunnable getObject(Trade trade) {
        return TradeRunnable.builder()
                .tradeId(trade.getTradeId())
                .interval(trade.getInterval())
                .tradeService(tradeService)
                .strategyService(strategyService)
                .brokerService(brokerService)
                .tradeExecutor(tradeExecutorFactory.getObject())
                .indiceClient(indiceClient)
                .brokerClientFactory(brokerClientFactory)
                .statusHandlerFactory(statusHandlerFactory)
                .transactionManager(transactionManager)
                .build();
    }

}
