package org.oopscraft.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketScriptRunnerFactory {

    private final AssetService assetService;

    private final OhlcvClient ohlcvClient;

    /**
     * gets object
     * @param basket basket
     * @return strategy runner
     */
    public BasketScriptRunner getObject(Basket basket) {
        switch (basket.getLanguage()) {
            case GROOVY -> {
                return GroovyBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            default -> throw new RuntimeException("invalid basket.language");
        }
    }

}
