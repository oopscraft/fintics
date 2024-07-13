package org.oopscraft.fintics.trade.basket;

import org.oopscraft.fintics.model.Basket;

public class BasketChangerFactory {

    /**
     * gets object
     * @param context strategy runner context
     * @return strategy runner
     */
    public BasketChanger getObject(BasketChangerContext context) {
        Basket basket = context.getBasket();
        switch (basket.getLanguage()) {
            case GROOVY -> {
                return GroovyBasketChanger.builder()
                        .basket(basket)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketChanger.builder()
                        .basket(basket)
                        .build();
            }
            default -> throw new RuntimeException("invalid basket.language");
        }
    }

}
