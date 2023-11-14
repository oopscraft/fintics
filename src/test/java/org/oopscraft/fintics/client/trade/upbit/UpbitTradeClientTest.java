package org.oopscraft.fintics.client.trade.upbit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OrderType;
import org.oopscraft.fintics.model.TradeAsset;

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

    Asset getTestAsset() {
        return Asset.builder()
                .symbol("XRP/KRW")
                .name("Ripple")
                .build();
    }

    @Disabled
    @Test
    void createJwtToken() {
        // given
        // when
        String jwtToken = getUpbitTradeClient().createJwtToken(null);

        // then
        log.info("jwtToken:{}", jwtToken);
    }

    @Disabled
    @Test
    void getOrderBook() {
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() throws Exception {
        // given
        Asset asset = getTestAsset();

        // when
        List<Ohlcv> minuteOhlcvs = getUpbitTradeClient().getMinuteOhlcvs(asset);

        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws Exception {
        // given
        Asset asset = getTestAsset();

        // when
        List<Ohlcv> dailyOhlcvs = getUpbitTradeClient().getDailyOhlcvs(asset);

        // then
        log.info("dailyOhlcvs:{}", dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws Exception {

    }

    @Disabled
    @Test
    void buyAsset() throws Exception {
        // given
        TradeAsset tradeAsset = Optional.of(getTestAsset())
                .map(asset -> TradeAsset.builder()
                        .symbol(asset.getSymbol())
                        .name(asset.getName())
                        .build())
                .orElseThrow();
        // when
        getUpbitTradeClient().buyAsset(tradeAsset, OrderType.MARKET, 1, null);
        // then
    }

    @Disabled
    @Test
    void sellAsset() {
    }

}