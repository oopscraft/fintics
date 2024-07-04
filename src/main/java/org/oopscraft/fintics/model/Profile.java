package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Builder.Default
    private final List<Ohlcv> dailyOhlcvs = new ArrayList<>();

    @Builder.Default
    private final List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private final List<News> newses = new ArrayList<>();

    /**
     * returns OHLCV list
     * @param type ohlcv type
     * @param period period
     * @return ohlcvs
     */
    public List<Ohlcv> getOhlcvs(Ohlcv.Type type, int period) {
        List<Ohlcv> ohlcvs;
        switch(type) {
            case MINUTE -> ohlcvs = resampleOhlcvs(minuteOhlcvs, period);
            case DAILY -> ohlcvs = resampleOhlcvs(dailyOhlcvs, period);
            default -> throw new IllegalArgumentException("invalid Ohlcv type");
        }
        return Collections.unmodifiableList(ohlcvs);
    }

    /**
     * resample ohlcvs by period
     * @param ohlcvs ohlcvs
     * @param period period
     * @return resampled ohlcvs
     */
    private List<Ohlcv> resampleOhlcvs(List<Ohlcv> ohlcvs, int period) {
        if (ohlcvs.isEmpty() || period <= 0) {
            return Collections.emptyList();
        }

        List<Ohlcv> resampledOhlcvs = new ArrayList<>();
        int dataSize = ohlcvs.size();
        int currentIndex = 0;

        while (currentIndex < dataSize) {
            int endIndex = Math.min(currentIndex + period, dataSize);
            List<Ohlcv> subList = ohlcvs.subList(currentIndex, endIndex);
            Ohlcv resampledData = createResampledOhlcv(subList);
            resampledOhlcvs.add(resampledData);
            currentIndex += period;
        }

        return resampledOhlcvs;
    }

    /**
     * creates resampled ohlcvs
     * @param ohlcvs ohlcvs
     * @return resampled ohlcvs
     */
    private Ohlcv createResampledOhlcv(List<Ohlcv> ohlcvs) {
        // convert to series
        List<Ohlcv> series = new ArrayList<>(ohlcvs);
        Collections.reverse(series);

        // merge ohlcv
        Ohlcv.Type type = null;
        LocalDateTime datetime = null;
        ZoneId timezone = null;
        BigDecimal open = BigDecimal.ZERO;
        BigDecimal high = BigDecimal.ZERO;
        BigDecimal low = BigDecimal.ZERO;
        BigDecimal close = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        for(int i = 0; i < series.size(); i ++ ) {
            Ohlcv ohlcv = series.get(i);
            if(i == 0) {
                type = ohlcv.getType();
                datetime = ohlcv.getDateTime();
                timezone = ohlcv.getTimeZone();
                open = ohlcv.getOpen();
                high = ohlcv.getHigh();
                low  = ohlcv.getLow();
                close = ohlcv.getClose();
                volume = ohlcv.getVolume();
            }else{
                datetime = ohlcv.getDateTime();
                if(ohlcv.getHigh().compareTo(high) > 0) {
                    high = ohlcv.getHigh();
                }
                if(ohlcv.getLow().compareTo(low ) < 0) {
                    low  = ohlcv.getLow();
                }
                close = ohlcv.getClose();
                volume = volume.add(ohlcv.getVolume());
            }
        }

        // return resampled ohlcvs
        return Ohlcv.builder()
                .type(type)
                .dateTime(datetime)
                .timeZone(timezone)
                .open(open)
                .high(high)
                .low(low )
                .close(close)
                .volume(volume)
                .build();
    }

}