package org.oopscraft.fintics.simulate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.dao.OhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class SimulateTradeClientTest extends CoreTestSupport {

    private static final String ASSET_ID = "test";

    private static final LocalDateTime NOW = LocalDateTime.now();

    private final OhlcvRepository assetOhlcvRepository;

    private final EntityManager entityManager;

    @BeforeEach
    void createTestData() {
        entityManager.persist(OhlcvEntity.builder()
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW)
                .open(BigDecimal.valueOf(300))
                .high(BigDecimal.valueOf(310))
                .low(BigDecimal.valueOf(305))
                .close(BigDecimal.valueOf(300))
                .build());
        entityManager.persist(OhlcvEntity.builder()
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW.minus(1, ChronoUnit.MINUTES))
                .open(BigDecimal.valueOf(200))
                .high(BigDecimal.valueOf(210))
                .low(BigDecimal.valueOf(205))
                .close(BigDecimal.valueOf(200))
                .build());
        entityManager.persist(OhlcvEntity.builder()
                .assetId(ASSET_ID)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(NOW.minus(2, ChronoUnit.MINUTES))
                .open(BigDecimal.valueOf(100))
                .high(BigDecimal.valueOf(110))
                .low(BigDecimal.valueOf(105))
                .close(BigDecimal.valueOf(100))
                .build());
        entityManager.flush();
    }

    SimulateBrokerClient createSimulateClient() {
        return SimulateBrokerClient.builder()
                .assetOhlcvRepository(assetOhlcvRepository)
                .build();
    }

    @Disabled
    @Test
    void deposit() {
        // given
        long amount = 1234;

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.deposit(BigDecimal.valueOf(amount));

        // then
        assertEquals(amount, tradeClient.getBalance().getCashAmount().longValue());
    }

    @Disabled
    @Test
    void getOrderBook() {
        // given
        Asset asset = Asset.builder()
                .assetId(ASSET_ID)
                .build();

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.setDatetime(NOW.minus(1, ChronoUnit.MINUTES));
        OrderBook orderBook = tradeClient.getOrderBook(asset);

        // then
        assertEquals(200, orderBook.getPrice().longValue());
    }

    @Disabled
    @Test
    void getBalance() {
        // given
        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.setDatetime(NOW.minus(1, ChronoUnit.MINUTES));
        Balance balance = tradeClient.getBalance();

        // then
        log.debug("balance:{}", balance);
    }

    @Disabled
    @Test
    void submitOrder() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .build();

        // when
        SimulateBrokerClient tradeClient = createSimulateClient();
        tradeClient.deposit(BigDecimal.valueOf(1000));
        tradeClient.setDatetime(NOW.minus(2, ChronoUnit.MINUTES));
        tradeClient.submitOrder(asset, Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());
        tradeClient.setDatetime(NOW);
        tradeClient.submitOrder(asset, Order.builder()
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