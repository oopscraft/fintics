package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.jpa.domain.Specification;

public class IndiceOhlcvSpecifications {

    public static Specification<IndiceOhlcvEntity> equalIndiceId(String indiceId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(IndiceOhlcvEntity_.INDICE_ID), indiceId));
    }

    public static Specification<IndiceOhlcvEntity> equalType(Ohlcv.Type type) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(IndiceOhlcvEntity_.TYPE), type));
    }

}
