package org.oopscraft.fintics.client.kis;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.AssetType;
import org.oopscraft.fintics.model.Balance;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class KisClientTest extends CoreTestSupport {

//    private final KisClient kisClient;
//
//    @Test
//    @Order(1)
//    void getAccessKey() {
//        // given
//        // when
//        String accessKey = kisClient.getAccessKey();
//
//        // then
//        assertNotNull(accessKey);
//    }
//
//    @Test
//    @Order(2)
//    void getBalance() {
//        // given
//        // when
//        Balance balance = kisClient.getBalance();
//
//        // then
//        assertNotNull(balance.getCash());
//    }
//
//    @Test
//    @Order(3)
//    void getAssetIndicator() {
//        // given
//        Asset asset = Asset.builder()
//                .symbol("069500")
//                .type(AssetType.STOCK)
//                .build();
//
//        // when
//        AssetIndicator assetIndicator = kisClient.getAssetIndicator(asset);
//
//        // then
//        assertNotNull(assetIndicator.getPrice());
//    }

}