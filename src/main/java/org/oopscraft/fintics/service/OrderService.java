package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.data.IdGenerator;
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

    public Order saveOrder(Order order) {
        OrderEntity orderEntity;
        if(order.getOrderId() != null) {
            orderEntity = orderRepository.findById(order.getOrderId()).orElseThrow();
        }else {
            orderEntity = OrderEntity.builder()
                    .orderId(IdGenerator.uuid())
                    .build();
        }
        orderEntity.setOrderAt(order.getOrderAt());
        orderEntity.setTradeId(order.getTradeId());
        orderEntity.setAssetId(order.getAssetId());
        orderEntity.setAssetName(order.getAssetName());
        orderEntity.setType(order.getType());
        orderEntity.setKind(order.getKind());
        orderEntity.setPrice(order.getPrice());
        orderEntity.setQuantity(order.getQuantity());
        orderEntity.setPurchasePrice(order.getPurchasePrice());
        orderEntity.setRealizedProfitAmount(order.getRealizedProfitAmount());
        orderEntity.setBrokerOrderId(order.getBrokerOrderId());
        orderEntity.setResult(order.getResult());
        orderEntity.setErrorMessage(order.getErrorMessage());
        OrderEntity savedOrderEntity = orderRepository.saveAndFlush(orderEntity);
        return Order.from(savedOrderEntity);
    }

}
