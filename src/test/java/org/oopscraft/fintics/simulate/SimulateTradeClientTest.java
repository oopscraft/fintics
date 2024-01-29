package org.oopscraft.fintics.simulate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvEntity;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class SimulateTradeClientTest extends CoreTestSupport {

    private static final String BROKER_ID = "KIS";

    private static final String ASSET_ID = "test";

    private static final LocalDateTime NOW = LocalDateTime.now();

    private final BrokerAssetOhlcvRepository assetOhlcvRepository;

    private final EntityManager entityManager;

    @BeforeEach
    void createTestData() {
        entityManager.persist(BrokerAssetOhlcvEntity.builder()
                .brokerId(BROKER_ID)
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW)
                .openPrice(BigDecimal.valueOf(300))
                .highPrice(BigDecimal.valueOf(310))
                .lowPrice(BigDecimal.valueOf(305))
                .closePrice(BigDecimal.valueOf(300))
                .build());
        entityManager.persist(BrokerAssetOhlcvEntity.builder()
                .brokerId(BROKER_ID)
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW.minusMinutes(1))
                .openPrice(BigDecimal.valueOf(200))
                .highPrice(BigDecimal.valueOf(210))
                .lowPrice(BigDecimal.valueOf(205))
                .closePrice(BigDecimal.valueOf(200))
                .build());
        entityManager.persist(BrokerAssetOhlcvEntity.builder()
                .brokerId(BROKER_ID)
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW.minusMinutes(2))
                .openPrice(BigDecimal.valueOf(100))
                .highPrice(BigDecimal.valueOf(110))
                .lowPrice(BigDecimal.valueOf(105))
                .closePrice(BigDecimal.valueOf(100))
                .build());
        entityManager.flush();
    }

    SimulateBrokerClient createSimulateClient() {
        return SimulateBrokerClient.builder()
                .brokerId(BROKER_ID)
                .assetOhlcvRepository(assetOhlcvRepository)
                .build();
    }

    @Test
    void deposit() throws Exception {
        // given
        long amount = 1234;

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.deposit(BigDecimal.valueOf(amount));

        // then
        assertEquals(amount, tradeClient.getBalance().getCashAmount().longValue());
    }


    @Test
    void getOrderBook() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId(ASSET_ID)
                .build();

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.setDateTime(NOW.minusMinutes(1));
        OrderBook orderBook = tradeClient.getOrderBook(asset);

        // then
        assertEquals(200, orderBook.getPrice().longValue());
    }

    @Test
    void getBalance() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId(ASSET_ID)
                .build();

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.setDateTime(NOW.minusMinutes(1));
        Balance balance = tradeClient.getBalance();

        // then
        log.debug("balance:{}", balance);
    }

    @Test
    void submitOrder() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .build();

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.deposit(BigDecimal.valueOf(1000));
        tradeClient.setDateTime(NOW.minusMinutes(2));
        tradeClient.submitOrder(Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());
        tradeClient.setDateTime(NOW);
        tradeClient.submitOrder(Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());

        // then
        Balance balance = tradeClient.getBalance();
        log.info("balance:{}", balance);
    }

}