package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {

    default Page<AssetEntity> findAllBy(AssetSearch assetSearch, Pageable pageable) {
        Specification<AssetEntity> specification = Specification.where(null);
        if(assetSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(AssetEntity_.ASSET_ID), '%' + assetSearch.getAssetId() + '%'));
        }
        if(assetSearch.getAssetName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(AssetEntity_.ASSET_NAME), '%' + assetSearch.getAssetName() + '%'));
        }
        if(assetSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(AssetEntity_.TYPE), assetSearch.getType()));
        }

        Sort sort = Sort.by(AssetEntity_.MARKET_CAP).descending();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageRequest);
    }

}
