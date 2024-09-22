package org.oopscraft.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.BasketAssetRepository;
import org.oopscraft.fintics.dao.BasketRepository;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.BasketService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketRebalanceTaskFactory {

    private final BasketService basketService;

    private final TradeService tradeService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    public BasketRebalanceTask getObject(Basket basket) {
        return BasketRebalanceTask.builder()
                .basket(basket)
                .basketService(basketService)
                .tradeService(tradeService)
                .basketScriptRunnerFactory(basketScriptRunnerFactory)
                .build();

    }

}
