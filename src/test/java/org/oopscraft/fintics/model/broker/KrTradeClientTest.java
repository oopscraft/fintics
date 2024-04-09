package org.oopscraft.fintics.model.broker;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.model.broker.KrBrokerClient;
import org.oopscraft.fintics.model.broker.KrBrokerClientDefinition;
import org.oopscraft.fintics.model.broker.BrokerClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class KrTradeClientTest {

    KrBrokerClientDefinition krTradeClientDefinition = new KrBrokerClientDefinition() {
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
        public String getConfigTemplate() {
            return null;
        }
    };

    KrBrokerClient krTradeClient = new KrBrokerClient(krTradeClientDefinition, null) {

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

    @Test
    void getPriceTickWithEtf() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .type("ETF")
                .build();
        BigDecimal price = BigDecimal.valueOf(30_000);
        // when
        BigDecimal etfPriceTick = krTradeClient.getPriceTick(asset, price);
        // then
        assertTrue(etfPriceTick.compareTo(BigDecimal.valueOf(5)) == 0);
    }

    @Test
    void getPriceTickStock() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .type("STOCK")
                .build();
        BigDecimal price = BigDecimal.valueOf(200_000);
        // when
        BigDecimal etfPriceTick = krTradeClient.getPriceTick(asset, price);
        // then
        assertTrue(etfPriceTick.compareTo(BigDecimal.valueOf(100)) == 0);
    }

}