package org.oopscraft.fintics.trade.basket;

import lombok.Builder;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketAsset;

import java.util.List;

public abstract class BasketChanger {

    private final Basket basket;

    protected BasketChanger(Basket basket) {
        this.basket = basket;
    }

    /**
     * select assets
     * @return selected assets
     */
    public abstract List<BasketAsset> getAssets();

}
