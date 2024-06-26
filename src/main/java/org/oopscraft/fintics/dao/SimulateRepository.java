package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.SimulateSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulateRepository extends JpaRepository<SimulateEntity, String>, JpaSpecificationExecutor<SimulateEntity> {

    default Page<SimulateEntity> findAll(SimulateSearch simulateSearch, Pageable pageable) {
        // where
        Specification<SimulateEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(simulateSearch.getTradeId())
                        .map(SimulateSpecifications::equalTradeId)
                        .orElse(null))
                .and(Optional.ofNullable(simulateSearch.getStatus())
                        .map(SimulateSpecifications::equalStatus)
                        .orElse(null))
                .and(Optional.ofNullable(simulateSearch.getFavorite())
                        .map(SimulateSpecifications::equalFavorite)
                        .orElse(null));

        // sort
        Sort sort = Sort.by(SimulateEntity_.STARTED_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        return findAll(specification, pageable);
    }

}
