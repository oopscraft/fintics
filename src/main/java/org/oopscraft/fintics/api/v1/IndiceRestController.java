package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.api.v1.dto.IndiceIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceResponse;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/indice")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('INDICE')")
@Tag(name = "indice", description = "Indice operations")
public class IndiceRestController {

    private final static String INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR = "IndiceRestController.getIndiceIndicator";

    private final IndiceService indiceService;

    @GetMapping
    public ResponseEntity<List<IndiceResponse>> getIndices() {
        List<IndiceResponse> indiceResponses = indiceService.getIndices().stream()
                .map(IndiceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indiceResponses);
    }

    @GetMapping("{symbol}")
    public ResponseEntity<IndiceResponse> getIndice(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceResponse indiceResponse = indiceService.getIndice(symbol)
                .map(IndiceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceResponse);
    }

    @Cacheable(cacheNames = INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR, key = "#symbol")
    @GetMapping("{symbol}/indicator")
    public ResponseEntity<IndiceIndicatorResponse> getIndiceIndicator(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceIndicatorResponse indiceIndicatorResponse = indiceService.getIndiceIndicator(symbol)
                .map(IndiceIndicatorResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceIndicatorResponse);
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    @PreAuthorize("permitAll()")
    @CacheEvict(cacheNames = INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR, allEntries = true)
    public void cacheEvictIndiceIndicator() {
        log.info("IndiceRestController.cacheEvictIndiceIndicator");
    }

}
