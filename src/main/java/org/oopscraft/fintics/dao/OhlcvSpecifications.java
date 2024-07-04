package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.AssetOhlcv;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class AssetOhlcvSpecifications {

    public static Specification<AssetOhlcvEntity> equalAssetId(String assetId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OhlcvEntity_.ASSET_ID), assetId));
    }

    public static Specification<AssetOhlcvEntity> equalType(AssetOhlcv.Type type) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OhlcvEntity_.TYPE), type));
    }

    public static Specification<AssetOhlcvEntity> betweenDatetime(Instant datetimeFrom, Instant datetimeTo) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(OhlcvEntity_.DATETIME), datetimeFrom, datetimeTo));
    }

}
