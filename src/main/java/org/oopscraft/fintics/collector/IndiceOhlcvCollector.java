package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndiceOhlcvCollector extends OhlcvCollector {

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void collect() {
        try {
            log.info("IndiceOhlcvCollector - Start collect indice ohlcv.");
            LocalDateTime dateTime = LocalDateTime.now();
            for (Indice.Id indiceId : Indice.Id.values()) {
                try {
                    saveIndiceMinuteOhlcvs(indiceId, dateTime);
                    saveIndiceDailyOhlcvs(indiceId, dateTime);
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("IndiceOhlcvCollector - End collect indice ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveIndiceMinuteOhlcvs(Indice.Id indiceId, LocalDateTime dateTime) {
        // current
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(indiceId, dateTime);
        List<IndiceOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(indiceOhlcv -> toIndiceOhlcvEntity(indiceId, indiceOhlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = minuteOhlcvs.get(0).getDateTime();
        List<IndiceOhlcvEntity> previousMinuteOhlcvEntities = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<IndiceOhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteOhlcvEntities);
        String unitName = String.format("indiceMinuteOhlcvEntities[%s]", indiceId);
        log.info("IndiceOhlcvCollector - save {}:{}", unitName, newOrChangedMinuteOhlcvEntities.size());
        saveEntities(unitName, newOrChangedMinuteOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    private void saveIndiceDailyOhlcvs(Indice.Id indiceId, LocalDateTime dateTime) {
        // current
        List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(indiceId, dateTime);
        List<IndiceOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .map(ohlcv -> toIndiceOhlcvEntity(indiceId, ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = dailyOhlcvs.get(0).getDateTime();
        List<IndiceOhlcvEntity> previousDailyOhlcvEntities = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<IndiceOhlcvEntity> newOrChangedDailyOhlcvEntities = extractNewOrChangedOhlcvEntities(dailyOhlcvEntities, previousDailyOhlcvEntities);
        String unitName = String.format("indiceDailyOhlcvEntities[%s]", indiceId);
        log.info("IndiceOhlcvCollector - save {}:{}", unitName, newOrChangedDailyOhlcvEntities.size());
        saveEntities(unitName, newOrChangedDailyOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    private IndiceOhlcvEntity toIndiceOhlcvEntity(Indice.Id indiceId, Ohlcv ohlcv) {
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

}
