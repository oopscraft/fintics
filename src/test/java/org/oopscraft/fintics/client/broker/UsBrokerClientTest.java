package org.oopscraft.fintics.client.broker;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class UsBrokerClientTest {

    UsBrokerClientDefinition usTradeClientDefinition = new UsBrokerClientDefinition() {
        @Override
        public String getBrokerClientId() {
            return null;
        }

        @Override
        public String getBrokerClientName() {
            return null;
        }

        @Override
        public Class<? extends BrokerClient> getClassType() {
            return null;
        }

        @Override
        public String getPropertiesTemplate() {
            return null;
        }
    };

    UsBrokerClient usTradeClient = new UsBrokerClient(usTradeClientDefinition, null) {

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
    @Test
    void getStockAssets() {
        // given
        // when
        List<Asset> assets = usTradeClient.getStockAssets();
        // then
        log.info("assets.size:{}", assets.size());
        assertTrue(assets.size() > 0);
    }

    @Disabled
    @Test
    void getEtfAssets() {
        // given
        // when
        List<Asset> assets = usTradeClient.getEtfAssets();
        // then
        log.info("assets.size:{}", assets.size());
        assertTrue(assets.size() > 0);
    }

}