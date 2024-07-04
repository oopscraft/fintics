package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.OhlcvResponse;
import org.oopscraft.fintics.service.OhlcvService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ohlcvs")
@RequiredArgsConstructor
public class OhlcvRestController {

    private final OhlcvService ohlcvService;

    @GetMapping("{assetId}/daily")
    public ResponseEntity<List<OhlcvResponse>> getDailyOhlcvs(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "datetimeFrom", required = false) Instant datetimeFrom,
            @RequestParam(value = "datetimeTo", required = false) Instant datetimeTo,
            Pageable pageable
    ) {
        datetimeFrom = Optional.ofNullable(datetimeFrom).orElse(Instant.MIN);
        datetimeTo = Optional.ofNullable(datetimeTo).orElse(Instant.MAX);
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getDailyOhlcvs(assetId, datetimeFrom, datetimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("daily-ohlcvs", pageable))
                .body(ohlcvResponses);
    }

    @GetMapping("{assetId}/minute")
    public ResponseEntity<List<OhlcvResponse>> getMinuteOhlcvs(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "datetimeFrom", required = false) Instant datetimeFrom,
            @RequestParam(value = "datetimeTo", required = false) Instant datetimeTo,
            Pageable pageable
    ) {
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getMinuteOhlcvs(assetId, datetimeFrom, datetimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("minute-ohlcvs", pageable))
                .body(ohlcvResponses);
    }

}
