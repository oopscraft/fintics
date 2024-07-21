package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecifications {

    public static Specification<OrderEntity> greaterThanOrEqualToOrderAt(Instant orderAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderAt);
    }

    public static Specification<OrderEntity> lessThanOrEqualToOrderAt(Instant orderAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderAt);
    }

    public static Specification<OrderEntity> equalTradeId(String tradeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), tradeId);
    }

    public static Specification<OrderEntity> likeAssetId(String assetId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(OrderEntity_.ASSET_ID), '%' + assetId + '%'));
    }

    public static Specification<OrderEntity> likeAssetName(String assetName) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(OrderEntity_.ASSET_NAME), '%' + assetName + '%'));
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
