package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
import org.springframework.beans.factory.annotation.Value;
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
public class IndiceCollector {

    @Value("${fintics.collector.indice-collector.ohlcv-retention-months:1}")
    private Integer ohlcvRetentionMonths = 1;

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectIndiceOhlcv() {
        log.info("Start collect indice ohlcv.");
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            try {
                saveIndiceOhlcv(symbol);
                deletePastRetentionOhlcv(symbol);
            }catch(Throwable e){
                log.warn(e.getMessage());
            }
        }
        log.info("End collect indice ohlcv");
    }

    private void saveIndiceOhlcv(IndiceSymbol symbol) {
        // minute
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol);
        Collections.reverse(minuteOhlcvs);
        LocalDateTime minuteLastDateTime = indiceOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(symbol, OhlcvType.MINUTE)
                .orElse(LocalDateTime.of(1,1,1,0,0,0))
                .minusMinutes(2);
        List<IndiceOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(minuteLastDateTime))
                .limit(30)
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(minuteOhlcvEntities);

        // daily
        List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol);
        Collections.reverse(dailyOhlcvs);
        LocalDateTime dailyLastDateTime = indiceOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(symbol, OhlcvType.DAILY)
                .orElse(LocalDateTime.of(1,1,1,0,0,0))
                .minusDays(2);
        List<IndiceOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                .limit(30)
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
    }

    private IndiceOhlcvEntity toIndiceOhlcvEntity(IndiceSymbol symbol, Ohlcv ohlcv) {
        return IndiceOhlcvEntity.builder()
                .symbol(symbol)
                .dateTime(ohlcv.getDateTime())
                .ohlcvType(ohlcv.getOhlcvType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private void deletePastRetentionOhlcv(IndiceSymbol symbol) {
        entityManager.createQuery(
                        "delete" +
                                " from IndiceOhlcvEntity" +
                                " where symbol = :symbol " +
                                " and dateTime < :expiredDateTime")
                .setParameter("symbol", symbol)
                .setParameter("expiredDateTime", LocalDateTime.now().minusMonths(ohlcvRetentionMonths))
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
