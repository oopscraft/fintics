package org.oopscraft.fintics.basket;

import lombok.Getter;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;

import java.util.List;

public abstract class BasketScriptRunner {

    @Getter
    protected final Basket basket;

    @Getter
    protected final AssetService assetService;

    @Getter
    protected final OhlcvClient ohlcvClient;

    protected BasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        this.basket = basket;
        this.assetService = assetService;
        this.ohlcvClient = ohlcvClient;
    }

    /**
     * get basket rebalance results
     * @return rebalance results
     */
    public abstract List<BasketRebalanceResult> run();

}
