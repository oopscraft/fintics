package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.web.support.PageableUtils;
import org.oopscraft.fintics.api.v1.dto.NewsResponse;
import org.oopscraft.fintics.service.NewsService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/newses")
@RequiredArgsConstructor
public class NewsRestController {

    private final NewsService newsService;

    @GetMapping("{assetId}")
    public ResponseEntity<List<NewsResponse>> getNewses(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "datetimeFrom", required = false) Instant datetimeFrom,
            @RequestParam(value = "datetimeTo", required = false) Instant datetimeTo,
            Pageable pageable
    ) {
        datetimeFrom = Optional.ofNullable(datetimeFrom).orElse(Instant.MIN);
        datetimeTo = Optional.ofNullable(datetimeTo).orElse(Instant.MAX);
        List<NewsResponse> newsResponses = newsService.getNewses(assetId, datetimeFrom, datetimeTo, pageable).stream()
                .map(NewsResponse::from)
                .toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset-news", pageable))
                .body(newsResponses);
    }

}
