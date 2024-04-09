package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.broker.BrokerClientFactory;
import org.oopscraft.fintics.model.indice.IndiceClient;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeRunnableFactory {

    private final TradeRepository tradeRepository;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final IndiceClient indiceClient;

    private final BrokerClientFactory brokerClientFactory;

    private final PlatformTransactionManager transactionManager;

    public TradeRunnable getObject(Trade trade) {
        return TradeRunnable.builder()
                .tradeId(trade.getTradeId())
                .interval(trade.getInterval())
                .tradeRepository(tradeRepository)
                .tradeExecutor(tradeExecutorFactory.getObject())
                .indiceClient(indiceClient)
                .brokerClientFactory(brokerClientFactory)
                .transactionManager(transactionManager)
                .build();
    }

}
