package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.domain.Specification;

public class SimulateSpecifications {

    public static Specification<SimulateEntity> equalTradeId(String tradeId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(SimulateEntity_.TRADE_ID), tradeId));
    }

}
