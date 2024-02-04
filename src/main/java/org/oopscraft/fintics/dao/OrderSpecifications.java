package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

    public static Specification<OrderEntity> equalTradeId(String tradeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), tradeId);
    }

    public static Specification<OrderEntity> equalType(Order.Type type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.TYPE), type);
    }

    public static Specification<OrderEntity> equalResult(Order.Result result) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.RESULT), result));
    }

}