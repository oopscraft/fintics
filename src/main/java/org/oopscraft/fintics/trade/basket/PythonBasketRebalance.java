package org.oopscraft.fintics.trade.basket;

import lombok.Builder;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;

import java.util.List;

public class PythonBasketRebalance extends BasketRebalance {

    @Builder
    public PythonBasketRebalance(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        super(basket, assetService, ohlcvClient);
    }

    @Override
    public List<BasketChange> getChanges() {
        return null;
    }

}
