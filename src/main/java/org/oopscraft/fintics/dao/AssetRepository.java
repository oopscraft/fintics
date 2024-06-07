package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {

    default Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable) {
        Specification<AssetEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(assetSearch.getAssetId())
                        .map(AssetSpecifications::containsAssetId)
                        .orElse(null))
                .and(Optional.ofNullable(assetSearch.getAssetName())
                        .map(AssetSpecifications::containsAssetName)
                        .orElse(null))
                .and(Optional.ofNullable(assetSearch.getMarket())
                        .map(AssetSpecifications::equalMarket)
                        .orElse(null))
                .and(Optional.ofNullable(assetSearch.getFavorite())
                        .map(AssetSpecifications::isFavorite)
                        .orElse(null));
        if (assetSearch.getPerFrom()!= null && assetSearch.getPerTo()!= null) {
            specification = specification.and(AssetSpecifications.betweenPer(assetSearch.getPerFrom(), assetSearch.getPerTo()));
        }
        if (assetSearch.getRoeFrom() != null && assetSearch.getRoeTo() != null) {
            specification = specification.and(AssetSpecifications.betweenRoe(assetSearch.getRoeFrom(), assetSearch.getRoeTo()));
        }
        if (assetSearch.getRoaFrom() != null && assetSearch.getRoeTo() != null) {
            specification = specification.and(AssetSpecifications.betweenRoa(assetSearch.getRoaFrom(), assetSearch.getRoaTo()));
        }
        Sort sort = Sort.by(AssetEntity_.MARKET_CAP).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageable);
    }

}
