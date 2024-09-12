package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.BrokerSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, String>, JpaSpecificationExecutor<BrokerEntity> {

    /**
     * find broker list
     * @param brokerSearch broker search criteria
     * @param pageable pageable
     * @return page of broker entity
     */
    default Page<BrokerEntity> findAll(BrokerSearch brokerSearch, Pageable pageable) {
        // where
        Specification<BrokerEntity> specification = Specification.where(null);
        specification = specification.and(Optional.ofNullable(brokerSearch.getName())
                .map(BrokerSpecifications::containsBrokerName)
                .orElse(null));
        // sort
        Sort sort = pageable.getSort().and(Sort.by(Sort.Direction.ASC, BrokerEntity_.NAME));

        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<BrokerEntity> brokerEntities = findAll(specification, sort);
            return new PageImpl<>(brokerEntities, pageable, brokerEntities.size());
        }
    }

}
