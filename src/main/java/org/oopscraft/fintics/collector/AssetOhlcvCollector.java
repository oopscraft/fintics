package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetOhlcvCollector {

    private final FinticsProperties finticsProperties;

    private final TradeRepository tradeRepository;

    private final AssetOhlcvRepository tradeAssetOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collect() {
        log.info("Start collect trade asset ohlcv.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        for (TradeEntity tradeEntity : tradeEntities) {
            try {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    saveAssetOhlcv(trade, tradeAsset);
                    deletePastRetentionOhlcv(trade, tradeAsset);
                }
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        log.info("End collect trade asset ohlcv");
    }

    private void saveAssetOhlcv(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        LocalDateTime dateTime = LocalDateTime.now();

        // minutes
        List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
        Collections.reverse(minuteOhlcvs);
        LocalDateTime minuteLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeAsset.getTradeId(), tradeAsset.getSymbol(), OhlcvType.MINUTE)
                .orElse(getExpiredDateTime())
                .minusMinutes(2);
        List<AssetOhlcvEntity> minuteTradeAssetOhlcvEntities = minuteOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(minuteLastDateTime))
                .limit(10)
                .map(ohlcv -> toAssetOhlcvEntity(trade.getClientId(), tradeAsset.getSymbol(), ohlcv))
                .collect(Collectors.toList());
        tradeAssetOhlcvRepository.saveAllAndFlush(minuteTradeAssetOhlcvEntities);

        // daily
        List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
        Collections.reverse(dailyOhlcvs);
        LocalDateTime dailyLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeAsset.getTradeId(), tradeAsset.getSymbol(), OhlcvType.DAILY)
                .orElse(getExpiredDateTime())
                .minusDays(2);
        List<AssetOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                .limit(10)
                .map(ohlcv -> toAssetOhlcvEntity(trade.getClientId(), tradeAsset.getSymbol(), ohlcv))
                .collect(Collectors.toList());
        tradeAssetOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
    }

    private AssetOhlcvEntity toAssetOhlcvEntity(String clientId, String symbol, Ohlcv ohlcv) {
        return AssetOhlcvEntity.builder()
                .clientId(clientId)
                .symbol(symbol)
                .dateTime(ohlcv.getDateTime())
                .ohlcvType(ohlcv.getOhlcvType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private LocalDateTime getExpiredDateTime() {
        return LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths());
    }

    private void deletePastRetentionOhlcv(Trade trade, TradeAsset tradeAsset) {
        entityManager.createQuery(
                        "delete" +
                                " from AssetOhlcvEntity" +
                                " where clientId = :clientId " +
                                " and symbol = :symbol " +
                                " and dateTime < :expiredDateTime")
                .setParameter("clientId", trade.getClientId())
                .setParameter("symbol", tradeAsset.getSymbol())
                .setParameter("expiredDateTime", getExpiredDateTime())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
