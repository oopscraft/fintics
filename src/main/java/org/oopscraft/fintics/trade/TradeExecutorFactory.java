package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.service.AssetService;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.OrderService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeExecutorFactory {

    private final PlatformTransactionManager transactionManager;

    private final IndiceService indiceService;

    private final AssetService assetService;

    private final OrderService orderService;

    private final AlarmService alarmService;


    public TradeExecutor getObject() {
        return TradeExecutor.builder()
                .transactionManager(transactionManager)
                .indiceService(indiceService)
                .assetService(assetService)
                .orderService(orderService)
                .alarmService(alarmService)
                .build();
    }

}
