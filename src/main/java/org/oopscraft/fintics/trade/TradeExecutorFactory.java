package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.trade.order.OrderOperatorFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeExecutorFactory {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final BrokerAssetOhlcvRepository assetOhlcvRepository;

    private final OrderOperatorFactory orderOperatorFactory;

    public TradeExecutor getObject() {
        return TradeExecutor.builder()
                .indiceOhlcvRepository(indiceOhlcvRepository)
                .assetOhlcvRepository(assetOhlcvRepository)
                .orderOperatorFactory(orderOperatorFactory)
                .build();
    }

}
