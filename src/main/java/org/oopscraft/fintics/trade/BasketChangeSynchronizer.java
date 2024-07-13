package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.service.BasketService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BasketChangeSynchronizer {

    private final BasketService basketService;

    private final BasketChangerScheduler basketChangerScheduler;

    @Scheduled(initialDelay = 10_000, fixedDelay = 10_000)
    public void synchronize() {
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        for (Basket basket : baskets) {

        }
    }
}
