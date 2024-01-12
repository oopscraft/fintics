package org.oopscraft.fintics.simulate;

import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SimulateIndiceClient extends IndiceClient {

    @Setter
    @Getter
    private LocalDateTime dateTime = LocalDateTime.now();

    private final Map<IndiceId,List<Ohlcv>> minuteOhlcvsMap = new HashMap<>();

    private final Map<IndiceId,List<Ohlcv>> dailyOhlcvsMap = new HashMap<>();

    public void addMinuteOhlcvs(IndiceId indiceId, List<Ohlcv> minuteOhlcvs) {
        minuteOhlcvsMap.put(indiceId, minuteOhlcvs);
    }

    public void addDailyOhlcvs(IndiceId indiceId, List<Ohlcv> dailyOhlcvs) {
        dailyOhlcvsMap.put(indiceId, dailyOhlcvs);
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(IndiceId indiceId, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = dateTime.minusMinutes(0);
        List<Ohlcv> minuteOhlcvs = null;
        if(minuteOhlcvsMap.containsKey(indiceId)) {
            minuteOhlcvs = minuteOhlcvsMap.get(indiceId).stream()
                    .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                            && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                    .collect(Collectors.toList());
        }else{
            minuteOhlcvs = new ArrayList<>();
            for (LocalDateTime currentDateTime = dateTimeTo; !currentDateTime.isBefore(dateTimeFrom); currentDateTime = currentDateTime.minusMinutes(1)) {
                minuteOhlcvs.add(Ohlcv.builder()
                        .dateTime(currentDateTime)
                        .build());
            }
        }
        return minuteOhlcvs;
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(IndiceId indiceId, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = dateTime.minusDays(0);
        List<Ohlcv> dailyOhlcvs = null;
        if(dailyOhlcvsMap.containsKey(indiceId)) {
            dailyOhlcvs = dailyOhlcvsMap.get(indiceId).stream()
                    .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                            && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                    .collect(Collectors.toList());
        }else{
            dailyOhlcvs = new ArrayList<>();
            for (LocalDateTime currentDateTime = dateTimeTo; !currentDateTime.isBefore(dateTimeFrom); currentDateTime = currentDateTime.minusDays(1)) {
                dailyOhlcvs.add(Ohlcv.builder()
                        .dateTime(currentDateTime)
                        .build());
            }
        }
        return dailyOhlcvs;
    }

}
