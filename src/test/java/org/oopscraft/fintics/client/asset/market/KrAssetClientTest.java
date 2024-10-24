package org.oopscraft.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    public KrAssetClient getKrAssetClient() {
        return new KrAssetClient(assetClientProperties);
    }

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getKrAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
    }

    @Test
    void applyAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .build();
        // when
        getKrAssetClient().applyAssetDetail(asset);
        // then
        log.info("asset: {}", asset);
    }

    @Test
    void applyAssetDetailIfInvalidSymbol() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.00Invalid")
                .name("test")
                .market("KR")
                .type("STOCK")
                .build();
        // when
        getKrAssetClient().applyAssetDetail(asset);
        // then
        log.info("asset: {}", asset);
    }

}