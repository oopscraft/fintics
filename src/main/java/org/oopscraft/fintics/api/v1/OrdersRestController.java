package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.OrderResponse;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderSearch;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.OrderService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAuthority('API_ORDERS')")
@RequiredArgsConstructor
@Tag(name = "order", description = "Order operations")
@Slf4j
public class OrdersRestController {

    private final OrderService orderService;

    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @RequestParam(value = "tradeId", required = false) String tradeId,
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Order.Type type,
            @RequestParam(value = "result", required = false) Order.Result result,
            @PageableDefault Pageable pageable
    ) {
        OrderSearch orderSearch = OrderSearch.builder()
                .tradeId(tradeId)
                .assetId(assetId)
                .type(type)
                .result(result)
                .build();
        Page<Order> orderPage = orderService.getOrders(orderSearch, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        // set trade name
        orderResponses.forEach(orderResponse ->
                orderResponse.setTradeName(tradeService.getTrade(orderResponse.getTradeId())
                        .map(Trade::getTradeName)
                        .orElse("")));

        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("order", pageable, orderPage.getTotalElements()))
                .body(orderResponses);
    }

}
