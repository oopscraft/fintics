package org.oopscraft.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketRebalanceFactory {

    private final AssetService assetService;

    private final OhlcvClient ohlcvClient;

    /**
     * gets object
     * @param context strategy runner context
     * @return strategy runner
     */
    public BasketRebalance getObject(BasketRebalanceContext context) {
        Basket basket = context.getBasket();
        switch (basket.getLanguage()) {
            case GROOVY -> {
                return GroovyBasketRebalance.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketRebalance.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            default -> throw new RuntimeException("invalid basket.language");
        }
    }

}
