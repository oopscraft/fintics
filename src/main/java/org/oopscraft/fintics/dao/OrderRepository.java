package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.OrderSearch;
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
public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    /**
     * finds order entities by order search
     * @param orderSearch order search
     * @param pageable pageable
     * @return page of order entity
     */
    default Page<OrderEntity> findAll(OrderSearch orderSearch, Pageable pageable) {
        // where
        Specification<OrderEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(orderSearch.getOrderAtFrom())
                        .map(OrderSpecifications::greaterThanOrEqualToOrderAt)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getOrderAtTo())
                        .map(OrderSpecifications::lessThanOrEqualToOrderAt)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getTradeId())
                        .map(OrderSpecifications::equalTradeId)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getAssetId())
                        .map(OrderSpecifications::likeAssetId)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getAssetName())
                        .map(OrderSpecifications::likeAssetName)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getType())
                        .map(OrderSpecifications::equalType)
                        .orElse(null))
                .and(Optional.ofNullable(orderSearch.getResult())
                        .map(OrderSpecifications::equalResult)
                        .orElse(null));

        // sort
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // return
        return findAll(specification, pageable);
    }
}
