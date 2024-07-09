package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.domain.Specification;

public class BasketSpecifications {

    public static Specification<BasketEntity> containsBasketName(String basketName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BasketEntity_.BASKET_NAME), '%' + basketName + '%');
    }

}
