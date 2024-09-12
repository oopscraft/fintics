package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.domain.Specification;

public class StrategySpecifications {

    public static Specification<StrategyEntity> containsRuleName(String strategyName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(StrategyEntity_.NAME), '%' + strategyName + '%');
    }

}
