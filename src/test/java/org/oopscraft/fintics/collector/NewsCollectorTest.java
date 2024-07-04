package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetNewsCollectorTest extends CoreTestSupport {

    private final NewsCollector newsCollector;

    @Test
    @Disabled
    void distinctNewsesByTitle() {
        // given
        List<News> newses = Stream.of("Title1", "Title1", "Title2")
                .map(title -> News.builder()
                        .datetime(Instant.now())
                        .title(title)
                        .build())
                .toList();
        // when
        List<News> distinctNewses = newsCollector.distinctAssetNewsesByTitle(newses);
        // then
        assertEquals(2, distinctNewses.size());
    }

}