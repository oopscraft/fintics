package org.oopscraft.fintics.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.news.NewsClient;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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

    @Scheduled(initialDelay = 10_000, fixedDelay = 3600_000)
    public void collect() {
        try {
            log.info("NewsCollector - Start collect news.");
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                if (trade.isEnabled()) {
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                        try {
                            if (tradeAsset.isEnabled()) {
                                collectAssetNews(tradeAsset);
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                            sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                        }
                    }
                }
            }
            // indice
            List<Indice> indices = indiceService.getIndices();
            for (Indice indice : indices) {
                try {
                    collectIndiceNews(indice);
                } catch (Exception e) {
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
        List<News> assetNewses = distinctNewsesByTitle(newsClient.getAssetNewses(asset));
        for (News assetNews : assetNewses) {
            try {
                String newsId = IdGenerator.md5(assetNews.getNewsUrl());
                AssetNewsEntity assetNewsEntity = assetNewsRepository.findById(AssetNewsEntity.Pk.builder()
                                .assetId(newsId)
                                .dateTime(assetNews.getDateTime())
                                .newsId(assetNews.getNewsId())
                                .build())
                        .orElse(null);
                if (assetNewsEntity == null) {
                    assetNewsEntity = AssetNewsEntity.builder()
                            .assetId(asset.getAssetId())
                            .dateTime(assetNews.getDateTime())
                            .newsId(newsId)
                            .newsUrl(assetNews.getNewsUrl())
                            .title(assetNews.getTitle())
                            .build();
                }

                // analysis
                if (assetNewsEntity.getSentiment() == null) {
                    analysisNews(assetNewsEntity);
                }

                // save news
                String unitName = String.format("assetNewsEntity[%s]: {}", asset.getAssetName(), assetNewsEntity.getTitle());
                saveEntities(unitName, List.of(assetNewsEntity), transactionManager, assetNewsRepository);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }

    void collectIndiceNews(Indice indice) {
        List<News> indiceNewses = distinctNewsesByTitle(newsClient.getIndiceNewses(indice));
        for (News indiceNews : indiceNewses) {
            try {
                String newsId = IdGenerator.md5(indiceNews.getNewsUrl());
                IndiceNewsEntity indiceNewsEntity = indiceNewsRepository.findById(IndiceNewsEntity.Pk.builder()
                                .indiceId(indice.getIndiceId())
                                .dateTime(indiceNews.getDateTime())
                                .newsId(indiceNews.getNewsId())
                                .build())
                        .orElse(null);
                if (indiceNewsEntity == null) {
                    indiceNewsEntity = IndiceNewsEntity.builder()
                            .indiceId(indice.getIndiceId())
                            .dateTime(indiceNews.getDateTime())
                            .newsId(newsId)
                            .newsUrl(indiceNews.getNewsUrl())
                            .title(indiceNews.getTitle())
                            .build();
                }

                // analysis
                if (indiceNewsEntity.getSentiment() == null) {
                    analysisNews(indiceNewsEntity);
                }

                // save news
                String unitName = String.format("indiceNewsEntity[%s]: %s", indice.getIndiceName(), indiceNewsEntity.getTitle());
                saveEntities(unitName, List.of(indiceNewsEntity), transactionManager, indiceNewsRepository);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }

    List<News> distinctNewsesByTitle(List<News> newses) {
        return new ArrayList<>(newses.stream()
                .collect(Collectors.toMap(
                        News::getTitle, // key
                        news -> news,   // value
                        (existing, replacement) -> existing // check existing
                ))
                .values());
    }

    void analysisNews(NewsEntity newsEntity) {
        // config not setting
        if (StringUtils.isBlank(finticsProperties.getAiApiUrl())) {
            return;
        }
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .readTimeout(60_000)
                    .build();
            String url = finticsProperties.getAiApiUrl() + "/news";
            Map<String,String> payload = new LinkedHashMap<>(){{
                put("url", newsEntity.getNewsUrl());
                put("title", newsEntity.getTitle());
            }};
            RequestEntity<Map<String,String>> requestEntity = RequestEntity
                    .post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            Map<String,String> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
            String sentiment = responseMap.get("sentiment");
            String confident = responseMap.get("confidence");
            String reason = responseMap.get("reason");
            newsEntity.setSentiment(News.Sentiment.valueOf(sentiment.toUpperCase(Locale.ROOT)));
            newsEntity.setConfidence(new BigDecimal(confident));
            newsEntity.setReason(reason);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

}
