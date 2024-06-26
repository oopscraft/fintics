package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.BrokerSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, String>, JpaSpecificationExecutor<BrokerEntity> {

    default Page<BrokerEntity> findAll(BrokerSearch brokerSearch, Pageable pageable) {
        Specification<BrokerEntity> specification = Specification.where(null);
        specification = specification.and(Optional.ofNullable(brokerSearch.getBrokerName())
                .map(BrokerSpecifications::containsBrokerName)
                .orElse(null));
        return findAll(specification, pageable);
    }

}
