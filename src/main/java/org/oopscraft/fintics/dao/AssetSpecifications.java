package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Asset;
import org.springframework.data.jpa.domain.Specification;

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

}
