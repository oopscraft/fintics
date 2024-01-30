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
public interface BrokerAssetRepository extends JpaRepository<BrokerAssetEntity, BrokerAssetEntity.Pk>, JpaSpecificationExecutor<BrokerAssetEntity> {

    default Page<BrokerAssetEntity> findAllBy(String brokerId, AssetSearch assetSearch, Pageable pageable) {
        Specification<BrokerAssetEntity> specification = Specification.where(null);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(BrokerAssetEntity_.BROKER_ID), brokerId));

        if(assetSearch.getAssetId() != null) {
            specification = specification.and(((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BrokerAssetEntity_.ASSET_ID), '%' + assetSearch.getAssetId() + '%')));
        }

        if(assetSearch.getAssetName() != null) {
            specification = specification.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(BrokerAssetEntity_.ASSET_NAME), '%' + assetSearch.getAssetName() + '%')));
        }

        Sort sort = Sort.by(BrokerAssetEntity_.SYSTEM_UPDATED_AT).descending();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageRequest);
    }

}
