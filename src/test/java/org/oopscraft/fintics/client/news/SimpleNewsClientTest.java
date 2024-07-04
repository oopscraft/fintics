package org.oopscraft.fintics.client.asset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.news.NewsClientProperties;
import org.oopscraft.fintics.client.news.SimpleNewsClient;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleAssetNewsClientTest extends CoreTestSupport {

    private final NewsClientProperties newsClientProperties;

    SimpleNewsClient getSimpleNewsClient() {
        return new SimpleNewsClient(newsClientProperties);
    }

    @Disabled
    @Test
    void extractNewsUrl() {
        // given
        String url = "https://news.google.com/rss/articles/CBMiZmh0dHBzOi8vYXBuZXdzLmNvbS9hcnRpY2xlL2lzcmFlbC1wYWxlc3RpbmlhbnMtaGFtYXMtd2FyLWdhbnR6LWRpdmlkZS1jMmJkZDA2YWIwNmRmYTU5ZDNkYTEyZjEzYjgxMjdhN9IBAA?oc=5";
        // when
        String newsUrl = getSimpleNewsClient().extractNewsUrl(url);
        // then
        log.info("newsUrl: {}", newsUrl);
    }

    @Disabled
    @Test
    void getNewses() {
        // given
        String keyword = "삼성전자";
        Locale locale = new Locale("ko", "KR");
        // when
        List<News> newses = getSimpleNewsClient().getNewses(keyword, locale);
        // then
        log.info("newses: {}", newses);
    }

    @Disabled
    @Test
    void getAssetNews() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .assetName("삼성전자")
                .build();
        // when
        List<News> news = getSimpleNewsClient().getNewses(asset);
        // then
        log.info("news: {}", news);
    }

    @Disabled
    @Test
    void getAssetNewsKrEtf() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.251340")
                .assetName("KODEX 코스닥150선물인버스")
                .build();
        // when
        List<News> news = getSimpleNewsClient().getNewses(asset);
        // then
        log.info("news: {}", news);
    }

}