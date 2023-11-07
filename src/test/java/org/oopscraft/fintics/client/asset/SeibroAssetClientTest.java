package org.oopscraft.fintics.client.asset;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Asset;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeibroAssetClientTest {

    @Disabled
    @Test
    void getStockAssets() {
        // given
        AssetClient assetClient = new SeibroAssetClient();
        int offset = 15;
        int limit = 105;

        // when
        List<Asset> stockAssets = assetClient.getStockAssets(offset, limit);

        // then
        assertTrue(stockAssets.size() == limit);
    }

    @Disabled
    @Test
    void getEtfAssets() {
        // given
        AssetClient assetClient = new SeibroAssetClient();
        int offset = 10;
        int limit = 100;

        // when
        List<Asset> etpAssets = assetClient.getEtfAssets(offset, limit);

        // then
        assertTrue(etpAssets.size() == limit);
    }

}