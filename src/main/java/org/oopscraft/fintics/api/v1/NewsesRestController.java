package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.NewsResponse;
import org.oopscraft.fintics.service.NewsService;
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
@RequestMapping("/api/v1/newses")
@PreAuthorize("hasAuthority('API_NEWSES')")
@Tag(name = "newses", description = "newses")
@RequiredArgsConstructor
public class NewsesRestController {

    private final NewsService newsService;

    /**
     * gets list of news
     * @param assetId asset id
     * @param datetimeFrom date time from
     * @param datetimeTo date time to
     * @param pageable pageable
     * @return list of news
     */
    @GetMapping("{assetId}")
    @Operation(description = "gets list of news")
    public ResponseEntity<List<NewsResponse>> getNewses(
            @PathVariable("assetId")
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "datetimeFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time from")
                    LocalDateTime datetimeFrom,
            @RequestParam(value = "datetimeTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "date time to")
                    LocalDateTime datetimeTo,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        // default parameter
        datetimeFrom = Optional.ofNullable(datetimeFrom)
                .orElse(LocalDate.of(1,1,1).atTime(LocalTime.MIN));
        datetimeTo = Optional.ofNullable(datetimeTo)
                .orElse(LocalDate.of(9999,12,31).atTime(LocalTime.MAX));
        // getting news
        List<NewsResponse> newsResponses = newsService.getNewses(assetId, datetimeFrom, datetimeTo, pageable).stream()
                .map(NewsResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("news", pageable))
                .body(newsResponses);
    }

}
