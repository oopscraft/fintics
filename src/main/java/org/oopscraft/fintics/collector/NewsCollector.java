package org.oopscraft.fintics.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.news.NewsClient;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCollector extends AbstractCollector {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final FinticsProperties finticsProperties;

    private final IndiceService indiceService;

    private final NewsClient newsClient;

    private final AssetNewsRepository assetNewsRepository;

    private final IndiceNewsRepository indiceNewsRepository;

    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void collect() {
        try {
            log.info("NewsCollector - Start collect news.");
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    try {
                        collectAssetNews(tradeAsset);
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                    }
                }
            }
            // indice
            List<Indice> indices = indiceService.getIndices();
            for (Indice indice : indices) {
                try {
                    collectIndiceNews(indice);
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", indice.getIndiceId(), indice.getIndiceName(), e.getMessage()));
                }
            }
            log.info("NewsCollector - End collect news");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void collectAssetNews(Asset asset) {
        List<News> assetNewses = newsClient.getAssetNewses(asset);
        List<AssetNewsEntity> assetNewsEntities = assetNewses.stream()
                .map(it -> AssetNewsEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(it.getDateTime())
                        .newsId(it.getNewsId())
                        .newsUrl(it.getNewsUrl())
                        .title(it.getTitle())
                        .sentiment(it.getSentiment())
                        .confidence(it.getConfidence())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("assetNewsEntities[%s]", asset.getAssetName());
        saveEntities(unitName, assetNewsEntities, transactionManager, assetNewsRepository);
    }

    void collectIndiceNews(Indice indice) {
        List<News> indiceNewses = newsClient.getIndiceNewses(indice);
        List<IndiceNewsEntity> indiceNewsEntities = indiceNewses.stream()
                .map(it -> IndiceNewsEntity.builder()
                        .indiceId(indice.getIndiceId())
                        .dateTime(it.getDateTime())
                        .newsId(it.getNewsId())
                        .newsUrl(it.getNewsUrl())
                        .title(it.getTitle())
                        .sentiment(it.getSentiment())
                        .confidence(it.getConfidence())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("indiceNewsEntities[%s]", indice.getIndiceName());
        saveEntities(unitName, indiceNewsEntities, transactionManager, indiceNewsRepository);
    }

}
