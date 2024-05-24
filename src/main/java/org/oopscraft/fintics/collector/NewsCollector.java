package org.oopscraft.fintics.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                    } catch (Exception e) {
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
        List<News> assetNewses = newsClient.getAssetNewses(asset);
        List<AssetNewsEntity> assetNewsEntities = new ArrayList<>();
        for (News assetNews : assetNewses) {
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
                assetNewsEntities.add(assetNewsEntity);
            }
        }

        // save news
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


    void analysisNews(NewsEntity newsEntity) {
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .readTimeout(60_000)
                    .build();
            String url = finticsProperties.getAiApiUrl() + "/news";
            String message = newsEntity.getTitle() + "\n" +
                    Optional.ofNullable(getNewsContent(newsEntity.getNewsUrl())).orElse("") + "\n";
            Map<String,String> payload = new LinkedHashMap<>(){{
                put("message", message);
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
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage());
        }
    }

    String getNewsContent(String newsUrl) {
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .build();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(newsUrl, String.class);
            String responseBody = responseEntity.getBody();
            Document doc = Jsoup.parse(responseBody);
            Element articleElement = doc.getElementsByTag("article").first();
            if (articleElement != null) {
                return articleElement.text();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
    }

}
