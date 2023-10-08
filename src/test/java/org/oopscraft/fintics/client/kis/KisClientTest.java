package org.oopscraft.fintics.client.kis;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;
import org.oopscraft.fintics.model.Balance;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.StringJoiner;

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

    @Test
    @Order(1)
    void getAccessKey() {
        // given
        // when
        String accessKey = getKisClient().getAccessKey();

        // then
        assertNotNull(accessKey);
    }

    @Test
    @Order(2)
    void getBalance() {
        // given
        // when
        Balance balance = getKisClient().getBalance();

        // then
        assertNotNull(balance.getCash());
    }

    @Test
    @Order(3)
    void getAssetIndicator() {
        // given
        // when
        AssetIndicator assetIndicator = getKisClient().getAssetIndicator("005930", AssetType.STOCK);

        // then
        assertNotNull(assetIndicator.getPrice());
    }

}