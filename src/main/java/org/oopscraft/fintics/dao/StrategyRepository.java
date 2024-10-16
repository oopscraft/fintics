package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.StrategySearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<StrategyEntity, String>, JpaSpecificationExecutor<StrategyEntity> {

    default Page<StrategyEntity> findAll(StrategySearch strategySearch, Pageable pageable) {
        Specification<StrategyEntity> specification = Specification.where(null);

        // name
        if (strategySearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(StrategyEntity_.NAME), '%' + strategySearch.getName() + '%'));
        }

        // find all
        return findAll(specification, pageable);
    }

}
