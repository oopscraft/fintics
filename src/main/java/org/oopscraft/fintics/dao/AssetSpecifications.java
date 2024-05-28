package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;

public class AssetSpecifications {

    public static Specification<AssetEntity> containsAssetId(String assetId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(AssetEntity_.ASSET_ID), '%' + assetId + '%');
    }

    public static Specification<AssetEntity> containsAssetName(String assetName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(AssetEntity_.ASSET_NAME), '%' + assetName + '%');
    }

    public static Specification<AssetEntity> equalMarket(String market) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(AssetEntity_.MARKET), market));
    }

    public static Specification<AssetEntity> isFavorite(Boolean favorite) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(AssetEntity_.FAVORITE), favorite);
    }

    public static Specification<AssetEntity> betweenPer(BigDecimal perFrom, BigDecimal perTo) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get(AssetEntity_.PER), perFrom));
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(AssetEntity_.PER), perTo));
            return predicate;
        };
    }

    public static Specification<AssetEntity> betweenRoe(BigDecimal roeFrom, BigDecimal roeTo) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get(AssetEntity_.ROE), roeFrom));
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(AssetEntity_.ROE), roeTo));
            return predicate;
        };
    }

    public static Specification<AssetEntity> betweenRoa(BigDecimal roaFrom, BigDecimal roaTo) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get(AssetEntity_.ROA), roaFrom));
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(AssetEntity_.ROA), roaTo));
            return predicate;
        };
    }

}
