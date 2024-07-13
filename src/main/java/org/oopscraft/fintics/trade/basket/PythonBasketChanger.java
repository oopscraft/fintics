package org.oopscraft.fintics.trade.basket;

import lombok.Builder;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketAsset;

import java.util.List;

public class PythonBasketChanger extends BasketChanger {

    @Builder
    public PythonBasketChanger(Basket basket) {
        super(basket);
    }

    @Override
    public List<BasketAsset> getAssets() {
        return null;
    }

}
