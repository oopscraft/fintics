package org.oopscraft.fintics.client.trade.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

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
    @Order(2)
    void getOrderBook() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
                .type(AssetType.STOCK)
                .build();

        // when
        OrderBook orderBook = getKisClient().getOrderBook(tradeAsset);
        log.info("== orderBook:{}", orderBook);

        // then
        assertNotNull(orderBook);
    }


    @Disabled
    @Test
    @Order(4)
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
                .type(AssetType.STOCK)
                .build();

        // when
        List<Ohlcv> minuteOhlcvs = getKisClient().getMinuteOhlcvs(tradeAsset);

        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    @Order(4)
    void getDailyOhlcvs() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
                .type(AssetType.STOCK)
                .build();

        // when
        List<Ohlcv> dailyOhlcvs = getKisClient().getDailyOhlcvs(tradeAsset);

        // then
        assertNotNull(dailyOhlcvs);
    }

    @Disabled
    @Test
    @Order(2)
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getKisClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    @Order(5)
    void buyAssetStock() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")           // Samsung Electronic
                .type(AssetType.STOCK)
                .build();

        // when
        getKisClient().buyAsset(tradeAsset, OrderType.MARKET, 1, null);
    }

    @Disabled
    @Test
    @Order(5)
    void buyAssetEtf() throws InterruptedException {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")           // KODEX 200
                .type(AssetType.ETF)
                .build();

        // when
        getKisClient().buyAsset(tradeAsset, OrderType.MARKET, 1, null);
    }

    @Disabled
    @Test
    @Order(5)
    void sellAssetStock() throws InterruptedException {
        // given
        BalanceAsset balanceAsset = BalanceAsset.builder()
                .symbol("005930")           // Samsung Electronic
                .type(AssetType.STOCK)
                .build();

        // when
        getKisClient().sellAsset(balanceAsset, OrderType.MARKET, 1, null);
    }

    @Disabled
    @Test
    @Order(5)
    void sellAssetEtf() throws InterruptedException {
        // given
        BalanceAsset balanceAsset = BalanceAsset.builder()
                .symbol("005930")           // KODEX 200
                .type(AssetType.ETF)
                .build();

        // when
        getKisClient().sellAsset(balanceAsset, OrderType.MARKET, 1, null);
    }


}