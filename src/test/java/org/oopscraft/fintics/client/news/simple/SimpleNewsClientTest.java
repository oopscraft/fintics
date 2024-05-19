package org.oopscraft.fintics.client.news.simple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.news.NewsClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleNewsClientTest extends CoreTestSupport {

    private final NewsClientProperties newsClientProperties;

    SimpleNewsClient getSimpleNewsClient() {
        return new SimpleNewsClient(newsClientProperties);
    }

    @Test
    void extractNewsUrl() {
        // given
        String url = "https://news.google.com/rss/articles/CBMiZmh0dHBzOi8vYXBuZXdzLmNvbS9hcnRpY2xlL2lzcmFlbC1wYWxlc3RpbmlhbnMtaGFtYXMtd2FyLWdhbnR6LWRpdmlkZS1jMmJkZDA2YWIwNmRmYTU5ZDNkYTEyZjEzYjgxMjdhN9IBAA?oc=5";
        // when
        String newsUrl = getSimpleNewsClient().extractNewsUrl(url);
        // then
        log.info("newsUrl: {}", newsUrl);
    }

    @Test
    void getNewses() {
        // given
        String keyword = "삼성전자";
        // when
        List<News> newses = getSimpleNewsClient().getNewses(keyword);
        // then
        log.info("newses: {}", newses);
    }

    @Test
    void getAssetNews() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .assetName("삼성전자")
                .build();
        // when
        List<News> news = getSimpleNewsClient().getAssetNewses(asset);
        // then
        log.info("news: {}", news);
    }

    @Test
    void getContent() {
        // given
        String newsUrl = "https://news.koreadaily.com/2024/05/18/economy/economygeneral/20240518140038357.html";
        // when
        String content = getSimpleNewsClient().getContent(newsUrl);
        // then
        log.info("content: {}", content);
    }

    @Test
    void analyzeNewsPositive() {
        // given
        News news = News.builder()
                .build();
        String content = "Samsung Electronics reported record profits for the last quarter, driven by strong sales of its new smartphones. Investors are optimistic about the company's future.";
        // when
        getSimpleNewsClient().analyzeNews(news, content);
        // then
    }

    @Test
    void analyzeNewsNegative() {
        // given
        News news = News.builder()
                .build();
        String content = "As the crisis of all-out war between Israel and Iran escalated, and even the U.S. consumption index exceeded expectations, the won-dollar exchange rate entered the 1,400 won range at one point on the 16th. This is the fourth time that the exchange rate has reached the 1,400 won range since the introduction of the floating exchange rate system in 1990. This is the result of the strengthening of the dollar as the forecast spread that the U.S. Central Bank (Fed)'s interest rate cut period will be reversed. While international oil prices (based on Brent crude oil) remain at the level of $90 per barrel, analysts say that uncertainty surrounding the Korean economy is growing as the KOSPI index plunged by more than 2% on this day. On this day, the won-dollar exchange rate rose by 10 won to 50 won (KRW The value fell) and closed at 1394 won50 ago. It rose sharply by 47 won and 40 won, changing its affiliated points for 7 consecutive trading days. The exchange rate rose to 1,400 won at 11:31 am.";
        content = "이스라엘과 이란 간 전면전 위기가 고조되는 가운데 미국의 소비지표마저 예상을 뛰어넘는 수치가 나오자 원·달러 환율이 16일 한때 1400원 선에 진입했다. 1990년 변동환율제도를 도입한 뒤 환율이 1400원대에 도달한 것은 이번이 네 번째다.미국 중앙은행(Fed)의 금리 인하 시기가 후퇴할 것이라는 전망이 확산하면서 달러화 강세 현상이 나타난 결과다. 국제 유가(브렌트유 기준)가 배럴당 90달러 수준을 유지하는 가운데 이날 코스피지수가 2% 넘게 급락하면서 한국 경제를 둘러싼 불확실성이 커지고 있다는 분석이 나온다.이날 원·달러 환율은 10원50전 상승(원화 가치는 하락)한 1394원50전에 마감했다. 7거래일 연속 연고점을 갈아치우며 47원40전 급등했다. 환율은 오전 11시31분께 1400원으로 올라섰다. 미국이 급격하게 금리를 올리던 시기인 2022년 11월 7일(1413원50전) 이후 약 17개월 만에 장중 1400원대에 진입했다. 이외에 원·달러 환율이 1400원대를 기록한 것은 1997년 외환위기, 2008년 글로벌 금융위기 시기다.유로화, 엔화 등 6개 통화 대비 달러 가치를 나타내는 달러인덱스는 전날 장중 106.366을 찍어 5개월여 만에 최고를 기록했다. 지난달 미국 소매판매가 전월보다 0.7% 증가한 7096억달러로, 시장 전망치(0.3% 증가)를 웃돈 결과다.이날 코스피지수는 2.28% 내린 2609.63으로 마감했다. 외국인 투자자가 2724억원어치를 순매도하며 하락세를 주도했다. 3년 만기 국고채 금리는 0.029%포인트 오른 연 3.469%에 장을 마쳤다. 일본 닛케이(-1.94%)와 홍콩 항셍지수(-2. 12%) 등 아시아 증시도 동반 하락했다.\n";
        // when
        getSimpleNewsClient().analyzeNews(news, content);
        // then
    }

}