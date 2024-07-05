package org.oopscraft.fintics.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class NewsCollectorTest extends CoreTestSupport {

    private final NewsCollector newsCollector;

    @Test
    @Disabled
    void distinctNewsesByTitle() {
        // given
        List<News> newses = Stream.of("Title1", "Title1", "Title2")
                .map(title -> News.builder()
                        .dateTime(LocalDateTime.now())
                        .title(title)
                        .build())
                .toList();
        // when
        List<News> distinctNewses = newsCollector.distinctNewsesByTitle(newses);
        // then
        assertEquals(2, distinctNewses.size());
    }

}