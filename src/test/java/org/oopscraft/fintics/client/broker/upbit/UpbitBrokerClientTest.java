package org.oopscraft.fintics.client.broker.upbit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Slf4j
class UpbitBrokerClientTest {

    private static final String accessKey;

    private static final String secretKey;

    static {
        accessKey = System.getenv("UPBIT_ACCESS_KEY");
        secretKey = System.getenv("UPBIT_SECRET_KEY");
    }

    UpbitBrokerClient getUpbitTradeClient() {
        Properties properties = new Properties();
        properties.setProperty("accessKey", accessKey);
        properties.setProperty("secretKey", secretKey);
        return new UpbitBrokerClient(new UpbitBrokerClientDefinition(), properties);
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .assetId("UPBIT.KRW-BTC")
                .assetName("Bitcoin")
                .build();
    }

    @Disabled
    @Test
    void getOrderBook() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();

        // when
        OrderBook orderBook = getUpbitTradeClient().getOrderBook(tradeAsset);

        // then
        log.info("orderBook:{}", orderBook);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();
        LocalDateTime fromDateTime = LocalDateTime.now().minusWeeks(1);
        LocalDateTime toDateTime = LocalDateTime.now();

        // when
        List<Ohlcv> minuteOhlcvs = getUpbitTradeClient().getMinuteOhlcvs(tradeAsset, LocalDateTime.now());

        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();
        LocalDate fromDate = LocalDate.now().minusYears(1);
        LocalDate toDate = LocalDate.now();

        // when
        List<Ohlcv> dailyOhlcvs = getUpbitTradeClient().getDailyOhlcvs(tradeAsset, LocalDateTime.now());

        // then
        log.info("dailyOhlcvs:{}", dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws Exception {
        // given
        // when
        Balance balance = getUpbitTradeClient().getBalance();
        // then
        log.info("balance: {}", balance);
    }

    @Disabled
    @Test
    void submitOrderBuy() throws Exception {
        // given
        Order order = Order.builder()
                .type(Order.Type.BUY)
                .assetId("UPBIT.KRW-BTC")
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(6))
                .price(BigDecimal.valueOf(840))
                .build();
        // when
        getUpbitTradeClient().submitOrder(order);
        // then
    }

    @Disabled
    @Test
    void submitOrderSell() throws Exception {
        // given
        Order order = Order.builder()
                .type(Order.Type.SELL)
                .assetId("UPBIT.KRW-BTC")
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(0.00008556))
                .price(null)
                .build();

        // when
        getUpbitTradeClient().submitOrder(order);
        // then
    }

    @Disabled
    @Test
    void getWaitingOrders() throws Exception {
        // given
        // when
        List<Order> orders = getUpbitTradeClient().getWaitingOrders();
        // then
        log.info("orders:{}", orders);
    }

    @Disabled
    @Test
    void amendOrder() throws Exception {
        // given
        List<Order> orders = getUpbitTradeClient().getWaitingOrders();

        // when
        for(Order order : orders) {
            Order amendedOrder = getUpbitTradeClient().amendOrder(order);
            log.debug("amendedOrder:{}", amendedOrder);
        }
        // then
    }

}