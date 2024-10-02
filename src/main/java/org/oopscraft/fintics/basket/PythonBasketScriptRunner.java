package org.oopscraft.fintics.basket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;
import org.oopscraft.fintics.strategy.StrategyResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PythonBasketScriptRunner extends BasketScriptRunner {

    @Builder
    public PythonBasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        super(basket, assetService, ohlcvClient);
    }

    @Override
    public List<BasketRebalanceAsset> run() {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            List<BasketRebalanceAsset> basketRebalanceAssets = new ArrayList<>();
            Value bindings = context.getBindings("python");
            bindings.putMember("basket", basket);
            bindings.putMember("asset_service", assetService);
            bindings.putMember("ohlcv_client", ohlcvClient);
            bindings.putMember("basket_rebalance_assets", basketRebalanceAssets);
            context.eval("python",
                    basket.getScript()
            );
            Value strategyResultValue = bindings.getMember("basket_rebalance_assets");
            if (!strategyResultValue.isNull()) {
                return basketRebalanceAssets;
            }
            return null;
        }
    }

}
