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

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    default Page<OrderEntity> findAll(OrderSearch orderSearch, Pageable pageable) {
        Specification<OrderEntity> specification = Specification.where(null);
        if(orderSearch.getTradeId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), orderSearch.getTradeId()));
        }
        if(orderSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.ASSET_ID), orderSearch.getAssetId()));
        }
        if(orderSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TYPE), orderSearch.getType()));
        }
        if(orderSearch.getResult() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.RESULT), orderSearch.getResult()));
        }
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageable);
    }

}
