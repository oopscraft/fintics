package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
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

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectIndiceOhlcv() throws InterruptedException {
        log.info("Start collect indice ohlcv.");
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            saveIndiceOhlcv(symbol);
        }
        entityManager.clear();
    }

    @Transactional
    public void saveIndiceOhlcv(IndiceSymbol symbol) {
        // minute
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol);
        Collections.reverse(minuteOhlcvs);
        LocalDateTime minuteLastDateTime = getLastDateTime(symbol, OhlcvType.MINUTE)
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
        LocalDateTime dailyLastDateTime = getLastDateTime(symbol, OhlcvType.DAILY)
                .minusDays(2);
        List<IndiceOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                .limit(30)
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
    }

    private LocalDateTime getLastDateTime(IndiceSymbol symbol, OhlcvType ohlcvType) {
        List<IndiceOhlcvEntity> latestRow = entityManager.createQuery(
                "select a from IndiceOhlcvEntity a " +
                        " where a.symbol = :symbol " +
                        " and a.ohlcvType = :ohlcvType " +
                        " order by a.dateTime desc",
                        IndiceOhlcvEntity.class)
                .setParameter("symbol", symbol)
                .setParameter("ohlcvType", ohlcvType)
                .setMaxResults(1)
                .getResultList();
        if(latestRow.isEmpty()) {
            return LocalDateTime.of(1,1,1,1,1,1);
        }else{
            return latestRow.get(0).getDateTime();
        }
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



}
