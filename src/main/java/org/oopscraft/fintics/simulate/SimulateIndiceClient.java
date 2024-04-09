package org.oopscraft.fintics.simulate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.model.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SimulateIndiceClient extends IndiceClient {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    @Setter
    @Getter
    private LocalDateTime dateTime = LocalDateTime.now();

    private final Map<IndiceId,List<Ohlcv>> minuteOhlcvsMap = new HashMap<>();

    private final Map<IndiceId,List<Ohlcv>> dailyOhlcvsMap = new HashMap<>();

    @Builder
    protected SimulateIndiceClient(IndiceOhlcvRepository indiceOhlcvRepository) {
        this.indiceOhlcvRepository = indiceOhlcvRepository;
    }

    private void loadOhlcvs(IndiceId indiceId, LocalDateTime dateTime) {
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE, dateTime.minusMonths(1), dateTime, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .toList();
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY, dateTime.minusYears(1), dateTime, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .toList();
        minuteOhlcvsMap.put(indiceId, minuteOhlcvs);
        dailyOhlcvsMap.put(indiceId, dailyOhlcvs);
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(IndiceId indiceId, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = dateTime.minusMinutes(0);
        if(minuteOhlcvsMap.containsKey(indiceId)) {
            return minuteOhlcvsMap.get(indiceId).stream()
                    .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                            && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                    .collect(Collectors.toList());
        }

        // fallback
        List<Ohlcv> minuteOhlcvs = new ArrayList<>();
        for (LocalDateTime currentDateTime = dateTimeTo; !currentDateTime.isBefore(dateTimeFrom); currentDateTime = currentDateTime.minusMinutes(1)) {
            minuteOhlcvs.add(Ohlcv.builder()
                    .dateTime(currentDateTime)
                    .build());
        }
        return minuteOhlcvs;
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(IndiceId indiceId, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = dateTime.minusDays(0);
        if(dailyOhlcvsMap.containsKey(indiceId)) {
            return dailyOhlcvsMap.get(indiceId).stream()
                    .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                            && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                    .collect(Collectors.toList());
        }

        // fallback
        List<Ohlcv> dailyOhlcvs = new ArrayList<>();
        for (LocalDateTime currentDateTime = dateTimeTo; !currentDateTime.isBefore(dateTimeFrom); currentDateTime = currentDateTime.minusDays(1)) {
            dailyOhlcvs.add(Ohlcv.builder()
                    .dateTime(currentDateTime)
                    .build());
        }
        return dailyOhlcvs;
    }

}
