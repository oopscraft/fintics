package org.oopscraft.fintics.basket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.service.BasketService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketRebalanceSynchronizer {

    private final BasketService basketService;

    private final BasketRebalanceScheduler basketChangerScheduler;

    @Scheduled(initialDelay = 10_000, fixedDelay = 10_000)
    public void synchronize() {
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        for (Basket basket : baskets) {
            try {
                if (basket.isRebalanceEnabled() && basket.getRebalanceSchedule() != null) {
                    Basket previousBasket = basketChangerScheduler.getScheduledBasket(basket.getBasketId());
                    // new
                    if (previousBasket == null) {
                        basketChangerScheduler.startScheduledTask(basket);
                        continue;
                    }
                    // changed
                    if (!Objects.equals(basket.getRebalanceSchedule(), previousBasket.getRebalanceSchedule())
                    || !Objects.equals(basket.getLanguage(), previousBasket.getLanguage())
                    || !Objects.equals(basket.getScript(), previousBasket.getScript())
                    ) {
                        basketChangerScheduler.startScheduledTask(basket);
                    }
                } else {
                    basketChangerScheduler.stopScheduledTask(basket);
                }
            } catch (Throwable e) {
                String errorMessage = String.format("basket schedule error[%s] - %s", basket.getName(), e.getMessage());
                log.warn(errorMessage);
            }
        }
    }
}
