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

    private final BasketRepository basketRepository;

    private final BasketAssetRepository basketAssetRepository;

    private final TradeService tradeService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    public BasketRebalanceTask getObject(Basket basket) {
        return BasketRebalanceTask.builder()
                .basket(basket)
                .basketRepository(basketRepository)
                .basketAssetRepository(basketAssetRepository)
                .tradeService(tradeService)
                .basketScriptRunnerFactory(basketScriptRunnerFactory)
                .build();

    }

}
