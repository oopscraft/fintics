package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.OrderResponse;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderSearch;
import org.oopscraft.fintics.model.OrderType;
import org.oopscraft.fintics.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ORDER')")
@Slf4j
public class OrderRestController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getTradeOrders(OrderSearch orderSearch, Pageable pageable) {
        Page<Order> orderPage = orderService.getOrders(orderSearch, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("order", pageable, orderPage.getTotalElements()))
                .body(orderResponses);
    }

}
