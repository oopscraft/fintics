package org.oopscraft.fintics.client.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.client.trade.kis.KisTradeClient;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KisTradeClientTest extends CoreTestSupport {

    private static final String TRADE_ID = "06c228451ce0400fa57bb36f0568d7cb";

    KisTradeClient getKisClient() {
        TradeEntity tradeEntity = entityManager.find(TradeEntity.class, TRADE_ID);
        String clientType = KisTradeClient.class.getName();
        String clientProperties = tradeEntity.getClientProperties();
        return (KisTradeClient) TradeClientFactory.getClient(clientType, clientProperties);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
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
                .symbol("005930")
                .build();

        // when
        List<Ohlcv> minuteOhlcvs = getKisClient().getMinuteOhlcvs(tradeAsset);

        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
                .build();

        // when
        List<Ohlcv> dailyOhlcvs = getKisClient().getDailyOhlcvs(tradeAsset);

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
                .symbol("005930")   // Samsung Electronic
                .orderKind(OrderKind.BUY)
                .orderType(OrderType.MARKET)
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
                .symbol("069500")   // Kodex 200 ETF
                .orderKind(OrderKind.BUY)
                .orderType(OrderType.MARKET)
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
                .symbol("005930")   // Samsung Electronic
                .orderKind(OrderKind.SELL)
                .orderType(OrderType.MARKET)
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
                .symbol("069500")   // Kodex 200 ETF
                .orderKind(OrderKind.SELL)
                .orderType(OrderType.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(order);
    }

}