package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.IndiceResponse;
import org.oopscraft.fintics.api.v1.dto.NewsResponse;
import org.oopscraft.fintics.api.v1.dto.OhlcvResponse;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/indices")
@PreAuthorize("hasAuthority('API_INDICES')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "indices", description = "Indices")
public class IndicesRestController {

    private final IndiceService indiceService;

    @GetMapping
    public ResponseEntity<List<IndiceResponse>> getIndices() {
        List<IndiceResponse> indiceResponses = indiceService.getIndices().stream()
                .map(IndiceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indiceResponses);
    }

    @GetMapping("{indiceId}")
    public ResponseEntity<IndiceResponse> getIndice(@PathVariable("indiceId") String indiceId) {
        IndiceResponse indiceResponse = indiceService.getIndice(indiceId)
                .map(IndiceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceResponse);
    }

    @GetMapping("{indiceId}/daily-ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getIndiceDailyOhlcvs(
            @PathVariable("indiceId") Indice.Id indiceId,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            @PageableDefault(size = 1000) Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(1000,1,1,0,0));
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(9999,12,31,23,59,59));
        List<OhlcvResponse> indiceOhlcvResponses = indiceService.getIndiceDailyOhlcvs(indiceId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

    @GetMapping("{indiceId}/minute-ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getIndiceMinuteOhlcvs(
            @PathVariable("indiceId") Indice.Id indiceId,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            @PageableDefault(size = 1000) Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(1000,1,1,0,0));
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(item -> item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.of(9999,12,31,23,59,59));
        List<OhlcvResponse> indiceOhlcvResponses = indiceService.getIndiceMinuteOhlcvs(indiceId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(indiceOhlcvResponses);
    }

    @RequestMapping("{indiceId}/newses")
    public ResponseEntity<List<NewsResponse>> getIndiceNewses(
            @PathVariable("indiceId") Indice.Id indiceId,
            @RequestParam(value = "dateTimeFrom", required = false) ZonedDateTime zonedDateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) ZonedDateTime zonedDateTimeTo,
            Pageable pageable
    ) {
        LocalDateTime dateTimeFrom = Optional.ofNullable(zonedDateTimeFrom)
                .map(it -> it.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.now().minusMonths(1));
        LocalDateTime dateTimeTo = Optional.ofNullable(zonedDateTimeTo)
                .map(it -> it.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.now());
        List<NewsResponse> newsResponses = indiceService.getIndiceNewses(indiceId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(NewsResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("indice-newses", pageable))
                .body(newsResponses);
    }

}
