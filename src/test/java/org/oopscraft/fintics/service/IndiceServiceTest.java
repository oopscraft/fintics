package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.IndiceNewsEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class IndiceServiceTest extends CoreTestSupport {

    private final IndiceService indiceService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Test
    void getIndices() {
        // given
        // when
        List<Indice> indices = indiceService.getIndices();
        // then
        assertTrue(indices.size() > 0);
    }

    @Test
    void getIndice() {
        // given
        String indiceId = Indice.Id.NDX.name();
        // when
        Indice indice = indiceService.getIndice(indiceId).orElseThrow();
        // then
        assertSame(Indice.Id.NDX, indice.getIndiceId());
    }

    @Test
    void getIndiceDailyOhlcvs() {
        // given
        Indice.Id indiceId = Indice.Id.NDX;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            IndiceOhlcvEntity indiceOhlcvEntity = IndiceOhlcvEntity.builder()
                    .indiceId(indiceId)
                    .type(Ohlcv.Type.DAILY)
                    .dateTime(dateTime)
                    .openPrice(BigDecimal.ONE)
                    .highPrice(BigDecimal.ONE)
                    .lowPrice(BigDecimal.ONE)
                    .closePrice(BigDecimal.ONE)
                    .volume(BigDecimal.ONE)
                    .build();
            entityManager.persist(indiceOhlcvEntity);
            entityManager.flush();
        });

        // when
        List<Ohlcv> indiceOhlcvs = indiceService.getIndiceDailyOhlcvs(indiceId, dateTimeFrom, dateTimeTo, Pageable.unpaged());
        // then
        log.info("indiceOhlcvs.size():{}", indiceOhlcvs.size());
        assertEquals(2, indiceOhlcvs.size());
    }

    @Test
    void getIndiceMinuteOhlcvs() {
        // given
        Indice.Id indiceId = Indice.Id.NDX;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            IndiceOhlcvEntity indiceOhlcvEntity = IndiceOhlcvEntity.builder()
                    .indiceId(indiceId)
                    .type(Ohlcv.Type.MINUTE)
                    .dateTime(dateTime)
                    .openPrice(BigDecimal.ONE)
                    .highPrice(BigDecimal.ONE)
                    .lowPrice(BigDecimal.ONE)
                    .closePrice(BigDecimal.ONE)
                    .volume(BigDecimal.ONE)
                    .build();
            entityManager.persist(indiceOhlcvEntity);
            entityManager.flush();
        });

        // when
        List<Ohlcv> indiceOhlcvs = indiceService.getIndiceMinuteOhlcvs(indiceId, dateTimeFrom, dateTimeTo, Pageable.unpaged());
        // then
        log.info("indiceOhlcvs.size():{}", indiceOhlcvs.size());
        assertEquals(2, indiceOhlcvs.size());
    }

    @Test
    void getIndiceNewses() {
        // given
        Indice.Id indiceId = Indice.Id.NDX;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            entityManager.persist(IndiceNewsEntity.builder()
                    .indiceId(indiceId)
                    .dateTime(dateTime)
                    .newsId(UUID.randomUUID().toString().replaceAll("-",""))
                    .build());
        });
        entityManager.flush();
        // when
        List<News> indiceNewses = indiceService.getIndiceNewses(indiceId, dateTimeFrom, dateTimeTo, PageRequest.of(0,10));
        // then
        assertTrue(indiceNewses.size() > 0);
    }

}