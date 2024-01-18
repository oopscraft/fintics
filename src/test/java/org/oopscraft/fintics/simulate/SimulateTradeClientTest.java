package org.oopscraft.fintics.simulate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SimulateTradeClientTest {

    List<Ohlcv> getTestMinuteOhlcvs(LocalDateTime now) {
        return new ArrayList<>(){{
            add(Ohlcv.builder()
                    .dateTime(now)
                    .openPrice(BigDecimal.valueOf(300))
                    .highPrice(BigDecimal.valueOf(310))
                    .lowPrice(BigDecimal.valueOf(305))
                    .closePrice(BigDecimal.valueOf(300))
                    .build());
            add(Ohlcv.builder()
                    .dateTime(now.minusMinutes(1))
                    .openPrice(BigDecimal.valueOf(200))
                    .highPrice(BigDecimal.valueOf(210))
                    .lowPrice(BigDecimal.valueOf(205))
                    .closePrice(BigDecimal.valueOf(200))
                    .build());
            add(Ohlcv.builder()
                    .dateTime(now.minusMinutes(2))
                    .openPrice(BigDecimal.valueOf(100))
                    .highPrice(BigDecimal.valueOf(110))
                    .lowPrice(BigDecimal.valueOf(105))
                    .closePrice(BigDecimal.valueOf(100))
                    .build());
        }};
    }

    @Test
    void deposit() throws Exception {
        // given
        long amount = 1234;

        // when
        SimulateTradeClient tradeClient = new SimulateTradeClient();
        tradeClient.deposit(BigDecimal.valueOf(amount));

        // then
        assertEquals(amount, tradeClient.getBalance().getCashAmount().longValue());
    }

    @Test
    void getOrderBook() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .build();
        LocalDateTime now = LocalDateTime.now();

        // when
        SimulateTradeClient tradeClient = new SimulateTradeClient();
        tradeClient.addMinuteOhlcvs(asset.getAssetId(), getTestMinuteOhlcvs(now));
        tradeClient.setDateTime(now.minusMinutes(1));
        OrderBook orderBook = tradeClient.getOrderBook(asset);

        // then
        assertEquals(200, orderBook.getPrice().longValue());
    }

    @Test
    void getBalance() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("test")
                .build();
        LocalDateTime now = LocalDateTime.now();

        // when
        SimulateTradeClient tradeClient = new SimulateTradeClient();
        tradeClient.addMinuteOhlcvs(asset.getAssetId(), getTestMinuteOhlcvs(now));
        tradeClient.setDateTime(now.minusMinutes(1));
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
        LocalDateTime now = LocalDateTime.now();

        // when
        SimulateTradeClient tradeClient = new SimulateTradeClient();
        tradeClient.addMinuteOhlcvs(asset.getAssetId(), getTestMinuteOhlcvs(now));
        tradeClient.deposit(BigDecimal.valueOf(1000));
        tradeClient.setDateTime(now.minusMinutes(2));
        tradeClient.submitOrder(Order.builder()
                .assetId(asset.getAssetId())
                .orderType(OrderType.BUY)
                .orderKind(OrderKind.MARKET)
                .quantity(BigDecimal.valueOf(2))
                .build());
        tradeClient.setDateTime(now);
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