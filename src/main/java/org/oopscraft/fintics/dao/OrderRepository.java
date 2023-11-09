package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.board.dao.BoardEntity;
import org.oopscraft.arch4j.core.board.dao.BoardEntity_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    default Page<OrderEntity> findAllByTradeId(String tradeId, Pageable pageable) {
        Specification<OrderEntity> specification = Specification.where(null);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), tradeId));
        return findAll(specification, pageable);
    }

}
