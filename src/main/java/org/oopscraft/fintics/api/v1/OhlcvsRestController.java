package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.common.data.PageableAsQueryParam;
import org.oopscraft.arch4j.web.common.data.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.OhlcvResponse;
import org.oopscraft.fintics.service.OhlcvService;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ohlcvs")
@PreAuthorize("hasAuthority('api.ohlcvs')")
@Tag(name = "ohlcvs", description = "ohlcvs operation")
@RequiredArgsConstructor
public class OhlcvsRestController {

    private final OhlcvService ohlcvService;

    /**
     * gets daily ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of ohlcv
     */
    @GetMapping("{assetId}/daily")
    @Operation(description = "gets daily ohlcvs")
    @PageableAsQueryParam
    public ResponseEntity<List<OhlcvResponse>> getDailyOhlcvs(
            @PathVariable("assetId")
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "dateTimeFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time from")
                    LocalDateTime dateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time to")
                    LocalDateTime dateTimeTo,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        // default parameter
        dateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(LocalDate.of(1,1,1).atTime(LocalTime.MIN));
        dateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(LocalDate.of(9999,12,31).atTime(LocalTime.MAX));
        // get ohlcvs
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getDailyOhlcvs(assetId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(ohlcvResponses);
    }

    /**
     * gets minute ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of minute ohlcv
     */
    @GetMapping("{assetId}/minute")
    @PageableAsQueryParam
    public ResponseEntity<List<OhlcvResponse>> getMinuteOhlcvs(
            @PathVariable("assetId")
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "dateTimeFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time from")
                    LocalDateTime dateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time to")
                    LocalDateTime dateTimeTo,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        // default parameter
        dateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(LocalDate.of(1,1,1).atTime(LocalTime.MIN));
        dateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(LocalDate.of(9999,12,31).atTime(LocalTime.MAX));
        // get ohlcvs
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getMinuteOhlcvs(assetId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(ohlcvResponses);
    }

}
