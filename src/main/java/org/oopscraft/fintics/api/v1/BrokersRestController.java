package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.BrokerRequest;
import org.oopscraft.fintics.api.v1.dto.BrokerResponse;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.BrokerSearch;
import org.oopscraft.fintics.service.BrokerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brokers")
@PreAuthorize("hasAuthority('API_BROKERS')")
@Tag(name = "brokers", description = "Brokers")
@RequiredArgsConstructor
public class BrokersRestController {

    private final BrokerService brokerService;

    /**
     * gets list of brokers
     * @param brokerName broker name
     * @param pageable pageable
     * @return list of brokers
     */
    @GetMapping
    @Operation(summary = "get list of brokers")
    public ResponseEntity<List<BrokerResponse>> getBrokers(
            @RequestParam(value = "brokerName", required = false)
            @Parameter(description = "broker name")
                    String brokerName,
            @PageableDefault
            @Parameter(hidden = true)
                    Pageable pageable
    ) {
        BrokerSearch brokerSearch = BrokerSearch.builder()
                .brokerName(brokerName)
                .build();
        Page<Broker> brokerPage = brokerService.getBrokers(brokerSearch, pageable);
        List<BrokerResponse> brokerResponses = brokerPage.getContent().stream()
                .map(BrokerResponse::from)
                .toList();
        long total = brokerPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("broker", pageable, total))
                .body(brokerResponses);
    }

    /**
     * gets broker
     * @param brokerId broker id
     * @return broker info
     */
    @GetMapping("{brokerId}")
    @Operation(summary = "gets broker info")
    public ResponseEntity<BrokerResponse> getBroker(
            @PathVariable("brokerId")
            @Parameter(description = "broker id")
                    String brokerId
    ) {
        BrokerResponse brokerResponse = brokerService.getBroker(brokerId)
                .map(BrokerResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(brokerResponse);
    }

    /**
     * creates broker
     * @param brokerRequest broker info
     * @return creates broker info
     */
    @PostMapping
    @PreAuthorize("hasAuthority('API_BROKERS_EDIT')")
    @Operation(summary = "creates new broker")
    public ResponseEntity<BrokerResponse> createBroker(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "broker request payload")
                    BrokerRequest brokerRequest
    ) {
        Broker broker = Broker.builder()
                .brokerName(brokerRequest.getBrokerName())
                .brokerClientId(brokerRequest.getBrokerClientId())
                .brokerClientProperties(brokerRequest.getBrokerClientProperties())
                .build();
        Broker savedBroker = brokerService.saveBroker(broker);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BrokerResponse.from(savedBroker));
    }

    /**
     * modifies broker info
     * @param brokerId broker id
     * @param brokerRequest broker info
     * @return modified broker info
     */
    @PutMapping("{brokerId}")
    @PreAuthorize("hasAuthority('API_BROKERS_EDIT')")
    @Operation(summary = "modifies specified broker info")
    public ResponseEntity<BrokerResponse> modifyBroker(
            @PathVariable("brokerId")
            @Parameter(description = "broker id")
                    String brokerId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "broker request payload")
                    BrokerRequest brokerRequest
    ) {
        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
        broker.setBrokerName(brokerRequest.getBrokerName());
        broker.setBrokerClientId(brokerRequest.getBrokerClientId());
        broker.setBrokerClientProperties(brokerRequest.getBrokerClientProperties());
        Broker savedBroker = brokerService.saveBroker(broker);
        return ResponseEntity.ok(BrokerResponse.from(savedBroker));
    }

    /**
     * deletes broker
     * @param brokerId broker id
     * @return void
     */
    @DeleteMapping("{brokerId}")
    @PreAuthorize("hasAuthority('API_BROKERS_EDIT')")
    @Operation(summary = "deletes specified broker")
    public ResponseEntity<Void> deleteBroker(
            @PathVariable("brokerId")
            @Parameter(name = "broker id", description = "broker id")
                    String brokerId
    ) {
        brokerService.deleteBroker(brokerId);
        return ResponseEntity.ok().build();
    }

}
