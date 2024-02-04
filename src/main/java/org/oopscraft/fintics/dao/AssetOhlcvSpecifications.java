package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.jpa.domain.Specification;

public class AssetOhlcvSpecifications {

    public static Specification<AssetOhlcvEntity> equalAssetId(String assetId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(AssetOhlcvEntity_.ASSET_ID), assetId));
    }

    public static Specification<AssetOhlcvEntity> equalType(Ohlcv.Type type) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(AssetOhlcvEntity_.TYPE), type));
    }

}
