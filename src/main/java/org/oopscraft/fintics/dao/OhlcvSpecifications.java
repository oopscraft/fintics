package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OhlcvSpecifications {

    public static Specification<OhlcvEntity> equalAssetId(String assetId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OhlcvEntity_.ASSET_ID), assetId));
    }

    public static Specification<OhlcvEntity> equalType(Ohlcv.Type type) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OhlcvEntity_.TYPE), type));
    }

    public static Specification<OhlcvEntity> betweenDatetime(Instant datetimeFrom, Instant datetimeTo) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(OhlcvEntity_.DATE_TIME), datetimeFrom, datetimeTo));
    }

}
