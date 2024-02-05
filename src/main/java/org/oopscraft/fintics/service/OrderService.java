package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderEntity_;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.dao.OrderSpecifications;
import org.oopscraft.fintics.model.Order;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<Order> getOrders(String tradeId, String assetId, Order.Type type, Order.Result result, Pageable pageable) {
        // where
        Specification<OrderEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(tradeId)
                        .map(OrderSpecifications::equalTradeId)
                        .orElse(null))
                .and(Optional.ofNullable(assetId)
                        .map(OrderSpecifications::equalAssetId)
                        .orElse(null))
                .and(Optional.ofNullable(type)
                        .map(OrderSpecifications::equalType)
                        .orElse(null))
                .and(Optional.ofNullable(result)
                        .map(OrderSpecifications::equalResult)
                        .orElse(null));

        // sort
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(specification, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .collect(Collectors.toList());
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

}
