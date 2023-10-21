package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.page.dao.PageEntity;
import org.oopscraft.arch4j.core.page.dao.PageEntity_;
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
public interface AssetRepository extends JpaRepository<AssetEntity,String>, JpaSpecificationExecutor<AssetEntity> {

    default Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable) {
        Specification<AssetEntity> specification = (root, query, criteriaBuilder) -> null;
        if(assetSearch.getSymbol() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(AssetEntity_.SYMBOL), '%' + assetSearch.getSymbol() + '%'));
        }
        if(assetSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(AssetEntity_.NAME), '%' + assetSearch.getName() + '%'));
        }
        if(assetSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(AssetEntity_.TYPE), assetSearch.getType()));
        }
        return findAll(specification, pageable);
    }

}
