package org.oopscraft.fintics.simulate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.trade.TradeExecutorFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SimulateRunnableFactory {

    private final IndiceService indiceService;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final PlatformTransactionManager transactionManager;

    private final SimulateRepository simulateRepository;

    private final ObjectMapper objectMapper;

    public SimulateRunnable getObject(Simulate simulate) {
        Trade trade = simulate.getTrade();

        SimulateIndiceClient simulateIndiceClient = SimulateIndiceClient.builder()
                .indiceOhlcvRepository(indiceOhlcvRepository)
                .build();

        SimulateTradeClient simulateTradeClient = SimulateTradeClient.builder()
                .tradeClientId(trade.getTradeClientId())
                .assetOhlcvRepository(assetOhlcvRepository)
                .build();

        // return
        return SimulateRunnable.builder()
                .simulate(simulate)
                .simulateIndiceClient(simulateIndiceClient)
                .simulateTradeClient(simulateTradeClient)
                .tradeExecutorFactory(tradeExecutorFactory)
                .transactionManager(transactionManager)
                .simulateRepository(simulateRepository)
                .objectMapper(objectMapper)
                .build();
    }

}
