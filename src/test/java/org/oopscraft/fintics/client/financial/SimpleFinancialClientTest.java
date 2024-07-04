package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.financial.FinancialClientProperties;
import org.oopscraft.fintics.client.financial.SimpleFinancialClient;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Financial;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleAssetFinancialClientTest extends CoreTestSupport {

    private final FinancialClientProperties assetFinancialClientProperties;

    private final ObjectMapper objectMapper;

    SimpleFinancialClient getSimpleAssetFinancialClient() {
        return new SimpleFinancialClient(assetFinancialClientProperties, objectMapper);
    }

    @Test
    void getUsAssetFinancial() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .market("US")
                .type("STOCK")
                .build();
        // when
        Financial assetFinancial = getSimpleAssetFinancialClient().getUsAssetFinancial(asset);
        // then
        log.info("asserFinancial: {}", assetFinancial);
    }

    @Test
    void getKrAssetFinancial() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .market("KR")
                .type("STOCK")
                .build();
        // when
        Financial assetFinancial = getSimpleAssetFinancialClient().getKrAssetFinancial(asset);
        // then
        log.info("assetFinancial: {}", assetFinancial);
    }

}