package org.oopscraft.fintics.client.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.MarketIndicator;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class YahooMarketClientTest extends CoreTestSupport {

    private final ObjectMapper objectMapper;

    @Disabled
    @Test
    void getNdxIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getNdxIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getNdxFutureIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getNdxFutureIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getSpxIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getSpxIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getSpxFutureIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getSpxFutureIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getDjiIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getDjiIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getDjiFutureIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator marketIndicator = marketClient.getDjiFutureIndicator();

        // then
        assertNotNull(marketIndicator);
    }

    @Disabled
    @Test
    void getUsdKrwIndicator() throws InterruptedException {
        // given
        // when
        MarketClient marketClient = new YahooMarketClient(objectMapper);
        MarketIndicator krwIndicator = marketClient.getUsdKrwIndicator();

        // then
        assertNotNull(krwIndicator);
    }

}