package org.oopscraft.fintics.client.kis;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class KisClientTest extends CoreTestSupport {

    private static final String TRADE_ID = "06c228451ce0400fa57bb36f0568d7cb";

    KisClient getKisClient() {
        TradeEntity tradeEntity = entityManager.find(TradeEntity.class, TRADE_ID);
        String clientType = KisClient.class.getName();
        String clientProperties = tradeEntity.getClientProperties();
        return (KisClient) ClientFactory.getClient(clientType, clientProperties);
    }

    @Disabled
    @Test
    @Order(1)
    void getAccessKey() {
        // given
        // when
        String accessKey = getKisClient().getAccessToken();

        // then
        assertNotNull(accessKey);
    }

    @Disabled
    @Test
    @Order(2)
    void getBalance() {
        // given
        // when
        Balance balance = getKisClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    @Order(4)
    void getAssetIndicatorStock() {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")
                .type(AssetType.STOCK)
                .build();

        // when
        AssetIndicator assetIndicator = getKisClient().getAssetIndicator(tradeAsset);
        BigDecimal minuteMacd = assetIndicator.getMinuteMacd();
        BigDecimal dailyMacd = assetIndicator.getDailyMacd();
        BigDecimal minuteRsi = assetIndicator.getMinuteRsi();
        BigDecimal dailyRsi = assetIndicator.getDailyRsi();

        // then
        assertNotNull(minuteMacd);
        assertNotNull(dailyMacd);
        assertNotNull(minuteRsi);
        assertNotNull(dailyRsi);
    }

    @Disabled
    @Test
    @Order(4)
    void getAssetIndicatorEtf() {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("069500")
                .type(AssetType.ETF)
                .build();

        // when
        AssetIndicator assetIndicator = getKisClient().getAssetIndicator(tradeAsset);
        BigDecimal minuteMacd = assetIndicator.getMinuteMacd();
        BigDecimal dailyMacd = assetIndicator.getDailyMacd();
        BigDecimal minuteRsi = assetIndicator.getMinuteRsi();
        BigDecimal dailyRsi = assetIndicator.getDailyRsi();

        // then
        assertNotNull(minuteMacd);
        assertNotNull(dailyMacd);
        assertNotNull(minuteRsi);
        assertNotNull(dailyRsi);
    }

    @Disabled
    @Test
    @Order(5)
    void buyAssetStock() {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")           // Samsung Electronic
                .type(AssetType.STOCK)
                .build();

        // when
        getKisClient().buyAsset(tradeAsset, 1);
    }

    @Disabled
    @Test
    @Order(5)
    void buyAssetEtf() {
        // given
        TradeAsset tradeAsset = TradeAsset.builder()
                .symbol("005930")           // KODEX 200
                .type(AssetType.ETF)
                .build();

        // when
        getKisClient().buyAsset(tradeAsset, 1);
    }

    @Disabled
    @Test
    @Order(5)
    void sellAssetStock() {
        // given
        BalanceAsset balanceAsset = BalanceAsset.builder()
                .symbol("005930")           // Samsung Electronic
                .type(AssetType.STOCK)
                .build();

        // when
        getKisClient().sellAsset(balanceAsset, 1);
    }

    @Disabled
    @Test
    @Order(5)
    void sellAssetEtf() {
        // given
        BalanceAsset balanceAsset = BalanceAsset.builder()
                .symbol("005930")           // KODEX 200
                .type(AssetType.ETF)
                .build();

        // when
        getKisClient().sellAsset(balanceAsset, 1);
    }


}