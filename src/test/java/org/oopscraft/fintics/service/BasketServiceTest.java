package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.BasketEntity;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketAsset;
import org.oopscraft.fintics.model.BasketSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class BasketServiceTest extends CoreTestSupport {

    private final BasketService basketService;

    @Test
    void getBaskets() {
        // given
        BasketEntity basketEntity = BasketEntity.builder()
                .basketId("test")
                .basketName("test name")
                .build();
        entityManager.persist(basketEntity);
        // when
        BasketSearch basketSearch = BasketSearch.builder()
                .basketName("test")
                .build();
        Page<Basket> basketPage = basketService.getBaskets(basketSearch, Pageable.unpaged());
        // then
    }

    @Test
    void getBasket() {
        // given
        BasketEntity basketEntity = BasketEntity.builder()
                .basketId("test")
                .basketName("test name")
                .build();
        entityManager.persist(basketEntity);
        // when
        Basket basket = basketService.getBasket("test").orElseThrow();
        // then
        log.info("basket: {}", basket);
    }

}