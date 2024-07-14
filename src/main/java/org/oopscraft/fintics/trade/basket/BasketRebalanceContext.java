package org.oopscraft.fintics.trade.basket;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Basket;

@Builder
@Getter
public class BasketRebalanceContext {

    private final Basket basket;

}
