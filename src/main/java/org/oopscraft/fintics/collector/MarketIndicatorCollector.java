package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.market.MarketClient;
import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.service.MarketService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MarketIndicatorCollector {

    private final MarketClient marketClient;


//    @Scheduled(initialDelay = 3_000, fixedDelay = 360_000 * 24)
//    @Transactional
//    @CachePut(value = MarketService.CACHE_MARKET)
//    public Market collect() {
//        return Market.builder()
//                .ndxIndicator(marketClient.getNdxIndicator())
//                .ndxFutureIndicator(marketClient.getNdxFutureIndicator())
//                .spxIndicator(marketClient.getSpxIndicator())
//                .spxFutureIndicator(marketClient.getSpxFutureIndicator())
//                .djiIndicator(marketClient.getDjiIndicator())
//                .djiFutureIndicator(marketClient.getDjiFutureIndicator())
//                .kospiIndicator(marketClient.getKospiIndicator())
//                .usdKrwIndicator(marketClient.getUsdKrwIndicator())
//                .build();
//    }


}
