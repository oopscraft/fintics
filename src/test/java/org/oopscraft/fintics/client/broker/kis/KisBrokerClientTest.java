package org.oopscraft.fintics.client.broker.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.broker.kis.KisBrokerClient;
import org.oopscraft.fintics.client.broker.kis.KisBrokerClientDefinition;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KisBrokerClientTest extends CoreTestSupport {

    private static final String production;

    private static final String apiUrl;

    private static final String appKey;

    private static final String appSecret;

    private static final String accountNo;

    static {
        production = Optional.ofNullable(System.getenv("PRODUCTION")).orElse("false");
        apiUrl = Optional.ofNullable(System.getenv("API_URL")).orElse("https://openapivts.koreainvestment.com:29443");
        appKey = System.getenv("KIS_APP_KEY");
        appSecret = System.getenv("KIS_APP_SECRET");
        accountNo = System.getenv("KIS_ACCOUNT_NO");
    }

    KisBrokerClient getKisClient() {
        Properties properties = new Properties();
        properties.setProperty("production", production);
        properties.setProperty("apiUrl", apiUrl);
        properties.setProperty("appKey", appKey);
        properties.setProperty("appSecret", appSecret);
        properties.setProperty("accountNo", accountNo);
        return new KisBrokerClient(new KisBrokerClientDefinition(), properties);
    }

    @Disabled
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime dateTime = LocalDateTime.now();
        // when
        boolean opened = getKisClient().isOpened(dateTime);
        // then
        log.info("== opened:{}", opened);
    }

    @Disabled
    @Test
    void isHoliday() throws InterruptedException {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.of(now.getYear(), 12, 25, 0, 0, 0);
        // when
        boolean holiday = getKisClient().isHoliday(dateTime);
        // then
        log.info("== holiday: {}", holiday);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .assetId("KR.005930")
                .build();

        // when
        OrderBook orderBook = getKisClient().getOrderBook(tradeAsset);
        log.info("== orderBook:{}", orderBook);

        // then
        assertNotNull(orderBook);
    }


    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .assetId("KR.005930")
                .build();

        // when
        List<Ohlcv> minuteOhlcvs = getKisClient().getMinuteOhlcvs(tradeAsset, LocalDateTime.now());

        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .assetId("KR.005930")
                .build();

        // when
        List<Ohlcv> dailyOhlcvs = getKisClient().getDailyOhlcvs(tradeAsset, LocalDateTime.now());

        // then
        assertNotNull(dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getKisClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    void submitOrderBuyStock() throws InterruptedException {
        // given
        Order order = Order.builder()
                .assetId("KR.005930")   // Samsung Electronic
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

    @Disabled
    @Test
    void submitOrderBuyEtf() throws InterruptedException {
        // given
        Order order = Order.builder()
                .assetId("KR.069500")   // Kodex 200 ETF
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

    @Disabled
    @Test
    void submitOrderSellStock() throws InterruptedException {
        // given
        Order order = Order.builder()
                .assetId("KR.005930")   // Samsung Electronic
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

    @Disabled
    @Test
    void submitOrderSellEtf() throws InterruptedException {
        // given
        Order order = Order.builder()
                .assetId("KR.069500")   // Kodex 200 ETF
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

}