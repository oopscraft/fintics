package org.oopscraft.fintics.simulate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
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

    private static final String TRADE_CLIENT_ID = "KIS";

    private static final String ASSET_ID = "test";

    private static final LocalDateTime NOW = LocalDateTime.now();

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final EntityManager entityManager;

    @BeforeEach
    void createTestData() {
        entityManager.persist(AssetOhlcvEntity.builder()
                .tradeClientId(TRADE_CLIENT_ID)
                .assetId(ASSET_ID)
                .ohlcvType(OhlcvType.MINUTE)
                .dateTime(NOW)
                .openPrice(BigDecimal.valueOf(300))
                .highPrice(BigDecimal.valueOf(310))
                .lowPrice(BigDecimal.valueOf(305))
                .closePrice(BigDecimal.valueOf(300))
                .build());
        entityManager.persist(AssetOhlcvEntity.builder()
                .tradeClientId(TRADE_CLIENT_ID)
                .assetId(ASSET_ID)
                .ohlcvType(OhlcvType.MINUTE)
                .dateTime(NOW.minusMinutes(1))
                .openPrice(BigDecimal.valueOf(200))
                .highPrice(BigDecimal.valueOf(210))
                .lowPrice(BigDecimal.valueOf(205))
                .closePrice(BigDecimal.valueOf(200))
                .build());
        entityManager.persist(AssetOhlcvEntity.builder()
                .tradeClientId(TRADE_CLIENT_ID)
                .assetId(ASSET_ID)
                .ohlcvType(OhlcvType.MINUTE)
                .dateTime(NOW.minusMinutes(2))
                .openPrice(BigDecimal.valueOf(100))
                .highPrice(BigDecimal.valueOf(110))
                .lowPrice(BigDecimal.valueOf(105))
                .closePrice(BigDecimal.valueOf(100))
                .build());
        entityManager.flush();
    }

    SimulateTradeClient createSimulateClient() {
        return SimulateTradeClient.builder()
                .tradeClientId(TRADE_CLIENT_ID)
                .assetOhlcvRepository(assetOhlcvRepository)
                .build();
    }

    @Test
    void deposit() throws Exception {
        // given
        long amount = 1234;

        // when
        SimulateTradeClient tradeClient = createSimulateClient();
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
        SimulateTradeClient tradeClient = createSimulateClient();
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
        SimulateTradeClient tradeClient = createSimulateClient();
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
        SimulateTradeClient tradeClient = createSimulateClient();
        tradeClient.deposit(BigDecimal.valueOf(1000));
        tradeClient.setDateTime(NOW.minusMinutes(2));
        tradeClient.submitOrder(Order.builder()
                .assetId(asset.getAssetId())
                .orderType(OrderType.BUY)
                .orderKind(OrderKind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());
        tradeClient.setDateTime(NOW);
        tradeClient.submitOrder(Order.builder()
                .assetId(asset.getAssetId())
                .orderType(OrderType.SELL)
                .orderKind(OrderKind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());

        // then
        Balance balance = tradeClient.getBalance();
        log.info("balance:{}", balance);
    }

}