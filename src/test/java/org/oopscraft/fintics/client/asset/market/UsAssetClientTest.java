package org.oopscraft.fintics.client.asset.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UsAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    private final ObjectMapper objectMapper;

    public UsAssetClient getUsAssetClient() {
        return new UsAssetClient(assetClientProperties, objectMapper);
    }

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getUsAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
        List<Asset> exchangeNullAssets = assets.stream().filter(it -> it.getExchange() == null).toList();
        log.info("exchangeNullAssets: {}", exchangeNullAssets);
    }

    @Test
    void getEtfAssets() {
        // given
        // when
        List<Asset> assets = getUsAssetClient().getEtfAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
        List<Asset> exchangeNullAssets = assets.stream().filter(it -> it.getExchange() == null).toList();
        log.info("exchangeNullAssets: {}", exchangeNullAssets);
    }

    @Test
    void getAssetMetas() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .assetName("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        List<AssetMeta> assetMetas = getUsAssetClient().getAssetMetas(asset);
        // then
        log.info("assetMetas: {}", assetMetas);
    }
}