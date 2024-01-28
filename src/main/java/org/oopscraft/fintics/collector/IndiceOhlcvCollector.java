package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndiceOhlcvCollector {

    private final FinticsProperties finticsProperties;

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collect() {
        log.info("Start collect indice ohlcv.");
        for (IndiceId symbol : IndiceId.values()) {
            try {
                saveIndiceOhlcv(symbol);
                deletePastRetentionOhlcv(symbol);
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        log.info("End collect indice ohlcv");
    }

    private void saveIndiceOhlcv(IndiceId symbol) {
        LocalDateTime dateTime = LocalDateTime.now();

        // minute
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol, dateTime);
        Collections.reverse(minuteOhlcvs);
        LocalDateTime minuteLastDateTime = indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndType(symbol, Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.of(1,1,1,0,0,0))
                .minusMinutes(2);
        List<IndiceOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(minuteLastDateTime))
                .limit(10)
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(minuteOhlcvEntities);

        // daily
        List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol, dateTime);
        Collections.reverse(dailyOhlcvs);
        LocalDateTime dailyLastDateTime = indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndType(symbol, Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.of(1,1,1,0,0,0))
                .minusDays(2);
        List<IndiceOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                .limit(10)
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
    }

    private IndiceOhlcvEntity toIndiceOhlcvEntity(IndiceId indiceId, Ohlcv ohlcv) {
        return IndiceOhlcvEntity.builder()
                .indiceId(indiceId)
                .dateTime(ohlcv.getDateTime())
                .type(ohlcv.getType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private void deletePastRetentionOhlcv(IndiceId symbol) {
        entityManager.createQuery(
                        "delete" +
                                " from IndiceOhlcvEntity" +
                                " where indiceId = :symbol " +
                                " and dateTime < :expiredDateTime")
                .setParameter("symbol", symbol)
                .setParameter("expiredDateTime", LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths()))
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
