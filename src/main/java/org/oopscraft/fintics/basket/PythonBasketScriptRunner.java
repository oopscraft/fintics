package org.oopscraft.fintics.basket;

import lombok.Builder;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;

import java.util.List;

public class PythonBasketScriptRunner extends BasketScriptRunner {

    @Builder
    public PythonBasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        super(basket, assetService, ohlcvClient);
    }

    @Override
    public List<BasketRebalanceAsset> run() {
        return null;
    }

}
