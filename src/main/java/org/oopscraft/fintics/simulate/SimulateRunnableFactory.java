package org.oopscraft.fintics.simulate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.OhlcvRepository;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.trade.StatusHandlerFactory;
import org.oopscraft.fintics.trade.TradeExecutorFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulateRunnableFactory {

    private final OhlcvRepository assetOhlcvRepository;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final SimulateRepository simulateRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final StatusHandlerFactory statusHandlerFactory;

    public SimulateRunnable getObject(Simulate simulate) {
        SimulateBrokerClient simulateTradeClient = SimulateBrokerClient.builder()
                .assetOhlcvRepository(assetOhlcvRepository)
                .feeRate(simulate.getFeeRate())
                .build();
        // return
        return SimulateRunnable.builder()
                .simulate(simulate)
                .simulateTradeClient(simulateTradeClient)
                .tradeExecutorFactory(tradeExecutorFactory)
                .simulateRepository(simulateRepository)
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .statusHandlerFactory(statusHandlerFactory)
                .build();
    }

}
