package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.BrokerAssetSearch;
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

    default Page<BrokerAssetEntity> findAllBy(String brokerId, BrokerAssetSearch brokerAssetSearch, Pageable pageable) {
        Specification<BrokerAssetEntity> specification = Specification.where(null);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(BrokerAssetEntity_.BROKER_ID), brokerId));

        if(brokerAssetSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BrokerAssetEntity_.ASSET_ID), '%' + brokerAssetSearch.getAssetId() + '%'));
        }

        if(brokerAssetSearch.getAssetName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(BrokerAssetEntity_.ASSET_NAME), '%' + brokerAssetSearch.getAssetName() + '%'));
        }

        if(brokerAssetSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(BrokerAssetEntity_.TYPE), brokerAssetSearch.getType()));
        }

        Sort sort = Sort.by(BrokerAssetEntity_.MARKET_CAP).descending();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageRequest);
    }

}
