package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.api.v1.dto.IndiceIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceResponse;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/indice")
@RequiredArgsConstructor
@Slf4j
public class IndiceRestController {

    private final static String INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR = "IndiceRestController.getIndiceIndicator";

    private final IndiceService indiceService;

    private final CacheManager cacheManager;

    @RequestMapping
    public ResponseEntity<List<IndiceResponse>> getIndices() {
        List<IndiceResponse> indiceResponses = indiceService.getIndices().stream()
                .map(IndiceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indiceResponses);
    }

    @RequestMapping("{symbol}")
    public ResponseEntity<IndiceResponse> getIndice(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceResponse indiceResponse = indiceService.getIndice(symbol)
                .map(IndiceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceResponse);
    }

    @Cacheable(cacheNames = INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR, key = "#symbol")
    @RequestMapping("{symbol}/indicator")
    public ResponseEntity<IndiceIndicatorResponse> getIndiceIndicator(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceIndicatorResponse indiceIndicatorResponse = indiceService.getIndiceIndicator(symbol)
                .map(IndiceIndicatorResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceIndicatorResponse);
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    public void cacheIndiceIndicator() {
        log.info("IndiceRestController.cacheIndiceIndicator");
        Cache cache = cacheManager.getCache(INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR);
        if(cache != null) {
            indiceService.getIndices().forEach(indice -> {
                IndiceSymbol symbol = indice.getSymbol();
                cache.put(symbol, getIndiceIndicator(symbol));
            });
        }
    }

}
