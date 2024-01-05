package org.oopscraft.fintics.client.trade.upbit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

@Slf4j
class UpbitTradeClientTest {

    private static final String accessKey;

    private static final String secretKey;

    static {
        accessKey = System.getenv("UPBIT_ACCESS_KEY");
        secretKey = System.getenv("UPBIT_SECRET_KEY");
    }

    UpbitTradeClient getUpbitTradeClient() {
        Properties properties = new Properties();
        properties.setProperty("accessKey", accessKey);
        properties.setProperty("secretKey", secretKey);
        return new UpbitTradeClient(properties);
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .symbol("KRW-BTC")
                .name("Bitcoin")
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

        // when
        List<Ohlcv> minuteOhlcvs = getUpbitTradeClient().getMinuteOhlcvs(tradeAsset);

        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();

        // when
        List<Ohlcv> dailyOhlcvs = getUpbitTradeClient().getDailyOhlcvs(tradeAsset);

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
                .orderType(OrderType.BUY)
                .symbol("KRW-BTC")
                .orderKind(OrderKind.MARKET)
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
                .orderType(OrderType.SELL)
                .symbol("KRW-BTC")
                .orderKind(OrderKind.MARKET)
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