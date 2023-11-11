package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeAssetOhlcvCollector {

    private final TradeRepository tradeRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectTradeAssetOhlcv() throws InterruptedException {
        log.info("Start collect trade asset ohlcv.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        for(TradeEntity tradeEntity : tradeEntities) {
            Trade trade = Trade.from(tradeEntity);
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            trade.getTradeAssets().forEach(tradeAsset -> {
                saveTradeAssetOhlcv(tradeClient, tradeAsset);
            });
        }
    }

    @Transactional
    public void saveTradeAssetOhlcv(TradeClient tradeClient, TradeAsset tradeAsset) {
         try {
             List<TradeAssetOhlcvEntity> minuteOhlcvEntities = tradeClient.getMinuteOhlcvs(tradeAsset).stream()
                     .map(ohlcv -> toTradeAssetOhlcvEntity(tradeAsset, ohlcv))
                     .collect(Collectors.toList());
             tradeAssetOhlcvRepository.saveAllAndFlush(minuteOhlcvEntities);

             List<TradeAssetOhlcvEntity> dailyOhlcvEntities = tradeClient.getDailyOhlcvs(tradeAsset).stream()
                     .map(ohlcv -> toTradeAssetOhlcvEntity(tradeAsset, ohlcv))
                     .collect(Collectors.toList());
             tradeAssetOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
         }catch(InterruptedException ignored){}
    }

    private TradeAssetOhlcvEntity toTradeAssetOhlcvEntity(TradeAsset tradeAsset, Ohlcv ohlcv) {
        return TradeAssetOhlcvEntity.builder()
                .tradeId(tradeAsset.getTradeId())
                .symbol(tradeAsset.getSymbol())
                .dateTime(ohlcv.getDateTime())
                .ohlcvType(ohlcv.getOhlcvType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }


}
