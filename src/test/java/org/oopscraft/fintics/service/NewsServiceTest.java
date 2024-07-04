package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.checkerframework.common.util.report.qual.ReportUnqualified;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.NewsEntity;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
public class NewsServiceTest extends CoreTestSupport {

    private final NewsService newsService;

    @Test
    void getNewses() {
        // given
        String assetId = "test";
        LocalDateTime datetimeFrom = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime datetimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(datetimeFrom, datetimeTo).forEach(dateTime -> {
            entityManager.persist(NewsEntity.builder()
                    .assetId(assetId)
                    .dateTime(dateTime)
                    .newsId(UUID.randomUUID().toString().replaceAll("-",""))
                    .build());
        });
        entityManager.flush();
        // when
        List<News> newses = newsService.getNewses(assetId, datetimeFrom, datetimeTo, PageRequest.of(0, 10));
        // then
        assertTrue(newses.size() > 0);
    }


}
