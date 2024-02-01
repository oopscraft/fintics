package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.api.v1.dto.IndiceIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceResponse;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @GetMapping("{indiceId}")
    public ResponseEntity<IndiceResponse> getIndice(@PathVariable("indiceId") IndiceId indiceId) {
        IndiceResponse indiceResponse = indiceService.getIndice(indiceId)
                .map(IndiceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceResponse);
    }

    @GetMapping("{indiceId}/indicator")
    @Cacheable(cacheNames = INDICE_REST_CONTROLLER_GET_INDICE_INDICATOR, key = "#indiceId + '_' + #dateTimeFrom + '_' + #dateTimeTo")
    public ResponseEntity<IndiceIndicatorResponse> getIndiceIndicator(
            @PathVariable("indiceId") IndiceId indiceId,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
        IndiceIndicatorResponse indiceIndicatorResponse = indiceService.getIndiceIndicator(indiceId, dateTimeFrom, dateTimeTo)
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
