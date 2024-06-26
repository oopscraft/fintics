package org.oopscraft.fintics.simulate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.indice.IndiceClientProperties;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.trade.StatusHandlerFactory;
import org.oopscraft.fintics.trade.TradeExecutorFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulateRunnableFactory {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final SimulateRepository simulateRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final IndiceClientProperties indiceClientProperties;

    private final ObjectMapper objectMapper;

    private final StatusHandlerFactory statusHandlerFactory;

    public SimulateRunnable getObject(Simulate simulate) {
        SimulateIndiceClient simulateIndiceClient = SimulateIndiceClient.builder()
                .indiceClientProperties(indiceClientProperties)
                .indiceOhlcvRepository(indiceOhlcvRepository)
                .build();
        SimulateBrokerClient simulateTradeClient = SimulateBrokerClient.builder()
                .assetOhlcvRepository(assetOhlcvRepository)
                .feeRate(simulate.getFeeRate())
                .build();
        // return
        return SimulateRunnable.builder()
                .simulate(simulate)
                .simulateIndiceClient(simulateIndiceClient)
                .simulateTradeClient(simulateTradeClient)
                .tradeExecutorFactory(tradeExecutorFactory)
                .simulateRepository(simulateRepository)
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .statusHandlerFactory(statusHandlerFactory)
                .build();
    }

}
