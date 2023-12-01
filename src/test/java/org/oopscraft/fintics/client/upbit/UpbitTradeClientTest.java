package org.oopscraft.fintics.client.upbit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.client.upbit.UpbitTradeClient;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Slf4j
class UpbitTradeClientTest {

    private static final String accessKey;

    private static final String secretKey;

    static {
        accessKey = System.getenv("UPBIT_ACCESS_KEY");
        secretKey = System.getenv("UPBIT_SECRET_KEY");
    }

    UpbitTradeClient getUpbitTradeClient() {
        Properties properties = new Properties();
        properties.setProperty("accessKey", accessKey);
        properties.setProperty("secretKey", secretKey);
        return new UpbitTradeClient(properties);
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .symbol("KRW-XRP")
                .name("Ripple")
                .build();
    }

    @Disabled
    @Test
    void getOrderBook() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();

        // when
        OrderBook orderBook = getUpbitTradeClient().getOrderBook(tradeAsset);

        // then
        log.info("orderBook:{}", orderBook);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();

        // when
        List<TradeAssetOhlcv> minuteOhlcvs = getUpbitTradeClient().getMinuteOhlcvs(tradeAsset);

        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws Exception {
        // given
        TradeAsset tradeAsset = getTestTradeAsset();

        // when
        List<TradeAssetOhlcv> dailyOhlcvs = getUpbitTradeClient().getDailyOhlcvs(tradeAsset);

        // then
        log.info("dailyOhlcvs:{}", dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws Exception {
        // given
        // when
        Balance balance = getUpbitTradeClient().getBalance();
        // then
        log.info("balance: {}", balance);
    }

    @Disabled
    @Test
    void buyAsset() throws Exception {
        // given
        TradeAsset tradeAsset = Optional.of(getTestTradeAsset())
                .map(asset -> TradeAsset.builder()
                        .symbol(asset.getSymbol())
                        .name(asset.getName())
                        .build())
                .orElseThrow();
        // when
        getUpbitTradeClient().buyAsset(tradeAsset, OrderType.MARKET, BigDecimal.valueOf(6), BigDecimal.valueOf(840));
        // then
    }

    @Disabled
    @Test
    void sellAsset() throws Exception {
        // given
        BalanceAsset balanceAsset = Optional.of(getTestTradeAsset())
                .map(asset -> BalanceAsset.builder()
                        .symbol(asset.getSymbol())
                        .name(asset.getName())
                        .build())
                .orElseThrow();
        // when
        getUpbitTradeClient().sellAsset(balanceAsset, OrderType.MARKET, BigDecimal.valueOf(5.9), BigDecimal.valueOf(850));
        // then

    }

}