package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.model.StrategySearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<BasketEntity, String>, JpaSpecificationExecutor<BasketEntity> {

    default Page<BasketEntity> findAll(BasketSearch basketSearch, Pageable pageable) {
        Specification<BasketEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(basketSearch.getBasketName())
                        .map(BasketSpecifications::containsBasketName)
                        .orElse(null));
        return findAll(specification, pageable);
    }

}
