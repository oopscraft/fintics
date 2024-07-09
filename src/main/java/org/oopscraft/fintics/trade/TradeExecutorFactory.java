package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.service.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeExecutorFactory {

    private final PlatformTransactionManager transactionManager;

    private final AssetService assetService;

    private final BasketService basketService;

    private final OhlcvService ohlcvService;

    private final NewsService newsService;

    private final OrderService orderService;

    private final AlarmService alarmService;

    public TradeExecutor getObject() {
        return TradeExecutor.builder()
                .transactionManager(transactionManager)
                .assetService(assetService)
                .basketService(basketService)
                .ohlcvService(ohlcvService)
                .newsService(newsService)
                .orderService(orderService)
                .alarmService(alarmService)
                .build();
    }

}
