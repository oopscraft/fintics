package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<Order> getOrders(OrderSearch orderSearch, Pageable pageable) {
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .collect(Collectors.toList());
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

}
