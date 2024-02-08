package org.oopscraft.fintics.client.trade;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oopscraft.fintics.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class KrTradeClientTest {

    KrTradeClientDefinition krTradeClientDefinition = new KrTradeClientDefinition() {
        @Override
        public String getTradeClientId() {
            return null;
        }

        @Override
        public String getTradeClientName() {
            return null;
        }

        @Override
        public Class<? extends TradeClient> getClassType() {
            return null;
        }

        @Override
        public String getConfigTemplate() {
            return null;
        }
    };

    KrTradeClient krTradeClient = new KrTradeClient(krTradeClientDefinition, null) {

        @Override
        public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
            return false;
        }

        @Override
        public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
            return null;
        }

        @Override
        public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
            return null;
        }

        @Override
        public OrderBook getOrderBook(Asset asset) throws InterruptedException {
            return null;
        }

        @Override
        public Balance getBalance() throws InterruptedException {
            return null;
        }

        @Override
        public Order submitOrder(Order order) throws InterruptedException {
            return null;
        }

        @Override
        public List<Order> getWaitingOrders() throws InterruptedException {
            return null;
        }

        @Override
        public Order amendOrder(Order order) throws InterruptedException {
            return null;
        }
    };

    @Disabled
    @ParameterizedTest
    @CsvSource({"11","12"})
    void getStockAssetsByExchangeType(String exchangeType) {
        // given
        // when
        List<Asset> assets = krTradeClient.getStockAssetsByExchangeType(exchangeType);
        // then
        log.info("assets.size:{}", assets.size());
        assertTrue(assets.size() > 0);
    }

    @Disabled
    @Test
    void getEtfAssets() {
        // given
        // when
        List<Asset> assets = krTradeClient.getEtfAssets();
        // then
        log.info("assets.size:{}", assets.size());
        assertTrue(assets.size() > 0);
    }

}