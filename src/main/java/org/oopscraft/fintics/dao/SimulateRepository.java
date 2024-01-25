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

import java.util.Objects;

@Repository
public interface SimulateRepository extends JpaRepository<SimulateEntity, String>, JpaSpecificationExecutor<SimulateEntity> {

    default Page<SimulateEntity> findAll(SimulateSearch simulateSearch, Pageable pageable) {
        Specification<SimulateEntity> specification = Specification.where(null);
        if(simulateSearch.getTradeId() != null) {
            specification = specification.and(((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(SimulateEntity_.TRADE_ID), simulateSearch.getTradeId())
            ));
        }
        Sort sort = Sort.by(SimulateEntity_.STARTED_AT).descending();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageRequest);
    }

}
