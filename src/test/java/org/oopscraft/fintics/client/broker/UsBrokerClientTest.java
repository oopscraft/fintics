package org.oopscraft.fintics.client.broker;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        public BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
            return null;
        }

        @Override
        public boolean isOverMinimumOrderAmount(BigDecimal quantity, BigDecimal price) throws InterruptedException {
            return false;
        }

        @Override
        public Balance getBalance() throws InterruptedException {
            return null;
        }

        @Override
        public Order submitOrder(Asset asset, Order order) throws InterruptedException {
            return null;
        }

        @Override
        public List<Order> getWaitingOrders() throws InterruptedException {
            return null;
        }

        @Override
        public Order amendOrder(Asset asset, Order order) throws InterruptedException {
            return null;
        }
    };

    @Disabled
    @Test
    void getStockAssets() {
        // given
        // when
        List<Asset> assets = usTradeClient.getStockAssets("NASDAQ");
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

    @Disabled
    @Test
    void getExchangeMap() {
        // given
        List<String> symbols = Arrays.asList("QQQ","SPY","TLT","_INVALID_");
        // when
        Map<String, String> exchangeMap = usTradeClient.getExchangeMap(symbols);
        // then
        log.info("exchangeMap: {}", exchangeMap);
    }

    @Disabled
    @Test
    void fillStockAssetProperty()  throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        // when
        usTradeClient.fillStockAssetProperty(asset);
        // then
        log.info("asset: {}", asset);
    }

}