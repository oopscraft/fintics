package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndiceOhlcvCollector {

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectIndiceOhlcv() throws InterruptedException {
        log.info("Start collect market.");
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            saveIndiceOhlcv(symbol);
        }
    }

    @Transactional
    public void saveIndiceOhlcv(IndiceSymbol symbol) {
        List<IndiceOhlcvEntity> minuteOhlcvEntities = indiceClient.getMinuteOhlcvs(symbol).stream()
                .map(ohlcv -> toIndiceOhlcvEntity(symbol, ohlcv))
                .collect(Collectors.toList());
        indiceOhlcvRepository.saveAllAndFlush(minuteOhlcvEntities);

        List<IndiceOhlcvEntity> dailyOhlcvEntities = indiceClient.getDailyOhlcvs(symbol).stream()
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

}
