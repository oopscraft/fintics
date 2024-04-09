package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.BrokerResponse;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.service.BrokerService;
import org.springdoc.core.converters.models.DefaultPageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brokers")
@PreAuthorize("hasAuthority('API_BROKERS')")
@Tag(name = "brokers", description = "Brokers")
@RequiredArgsConstructor
public class BrokersRestController {

    private final BrokerService brokerService;

    @GetMapping
    public ResponseEntity<List<BrokerResponse>> getBrokers(
            @RequestParam(value = "brokerName", required = false) String brokerName,
            @PageableDefault Pageable pageable
    ) {
        Page<Broker> brokerPage = brokerService.getBrokers(brokerName, pageable);
        List<BrokerResponse> brokerResponses = brokerPage.getContent().stream()
                .map(BrokerResponse::from)
                .toList();
        long total = brokerPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("broker", pageable, total))
                .body(brokerResponses);
    }

    @GetMapping("{brokerId}")
    public ResponseEntity<BrokerResponse> getBroker(@PathVariable("brokerId") String brokerId) {
        BrokerResponse brokerResponse = brokerService.getBroker(brokerId)
                .map(BrokerResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(brokerResponse);
    }

}
