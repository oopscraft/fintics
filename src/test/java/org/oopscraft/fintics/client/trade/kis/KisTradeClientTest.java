package org.oopscraft.fintics.client.trade.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
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
class KisTradeClientTest extends CoreTestSupport {

    private static final String production;

    private static final String apiUrl;

    private static final String appKey;

    private static final String appSecret;

    private static final String accountNo;

    static {
        production = Optional.ofNullable(System.getenv("PRODUCTION")).orElse("false");
        apiUrl = Optional.ofNullable(System.getenv("API_URL")).orElse("https://openapivts.koreainvestment.com:29443");
        appKey = System.getenv("APP_KEY");
        appSecret = System.getenv("APP_SECRET");
        accountNo = System.getenv("ACCOUNT_NO");
    }

    KisTradeClient getKisClient() {
        Properties properties = new Properties();
        properties.setProperty("production", production);
        properties.setProperty("apiUrl", apiUrl);
        properties.setProperty("appKey", appKey);
        properties.setProperty("appSecret", appSecret);
        properties.setProperty("accountNo", accountNo);
        return new KisTradeClient(properties);
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
        assertTrue(holiday);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .id("005930")
                .build();

        // when
        OrderBook orderBook = getKisClient().getOrderBook(tradeAsset, LocalDateTime.now());
        log.info("== orderBook:{}", orderBook);

        // then
        assertNotNull(orderBook);
    }


    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .id("005930")
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
                .id("005930")
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
                .assetId("005930")   // Samsung Electronic
                .orderType(OrderType.BUY)
                .orderKind(OrderKind.MARKET)
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
                .assetId("069500")   // Kodex 200 ETF
                .orderType(OrderType.BUY)
                .orderKind(OrderKind.MARKET)
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
                .assetId("005930")   // Samsung Electronic
                .orderType(OrderType.SELL)
                .orderKind(OrderKind.MARKET)
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
                .assetId("069500")   // Kodex 200 ETF
                .orderType(OrderType.SELL)
                .orderKind(OrderKind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

}