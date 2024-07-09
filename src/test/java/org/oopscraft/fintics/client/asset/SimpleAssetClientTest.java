package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.client.ohlcv.SimpleOhlcvClient;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    private final ObjectMapper objectMapper;

    SimpleAssetClient getSimpleAssetClient() {
        return new SimpleAssetClient(assetClientProperties, objectMapper);
    }

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getSimpleAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
    }

    @Test
    void getUsAssetMetas() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .assetName("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        List<AssetMeta> assetMetas = getSimpleAssetClient().getAssetMetas(asset);
        // then
        log.info("assetMetas: {}", assetMetas);
    }

    @Test
    void getKrAssetMetas() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .assetName("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .build();
        // when
        List<AssetMeta> assetMetas = getSimpleAssetClient().getAssetMetas(asset);
        // then
        log.info("assetMetas: {}", assetMetas);
    }

}