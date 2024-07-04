package org.oopscraft.fintics.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AssetFinancialRepositoryCustomImpl implements AssetFinancialRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable) {
        return null;
    }
}
