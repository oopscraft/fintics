package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.calculator.CalculateContext;
import org.oopscraft.fintics.calculator.CalculateResult;
import org.oopscraft.fintics.calculator.Calculator;
import org.oopscraft.fintics.calculator.CalculatorFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuperBuilder
@Getter
public class TradeAssetIndicator {

    private final String symbol;

    private final String name;

    @Builder.Default
    @Getter
    private final List<TradeAssetOhlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    @Getter
    private final List<TradeAssetOhlcv> dailyOhlcvs = new ArrayList<>();

    public List<TradeAssetOhlcv> resample(OhlcvType ohlcvType, int period) {
        switch(ohlcvType) {
            case MINUTE -> {
                return resampleOhlcvs(minuteOhlcvs, period);
            }
            case DAILY -> {
                return resampleOhlcvs(dailyOhlcvs, period);
            }
            default -> throw new IllegalArgumentException("invalid Ohlcv type");
        }
    }

    private List<TradeAssetOhlcv> resampleOhlcvs(List<TradeAssetOhlcv> ohlcvs, int period) {
        if (ohlcvs.isEmpty() || period <= 0) {
            return Collections.emptyList();
        }

        List<TradeAssetOhlcv> resampledOhlcvs = new ArrayList<>();
        int dataSize = ohlcvs.size();
        int currentIndex = 0;

        while (currentIndex < dataSize) {
            int endIndex = Math.min(currentIndex + period, dataSize);
            List<TradeAssetOhlcv> subList = ohlcvs.subList(currentIndex, endIndex);
            TradeAssetOhlcv resampledData = createResampledOhlcv(subList);
            resampledOhlcvs.add(resampledData);
            currentIndex += period;
        }

        return resampledOhlcvs;
    }

    private TradeAssetOhlcv createResampledOhlcv(List<TradeAssetOhlcv> ohlcvs) {
        List<TradeAssetOhlcv> series = new ArrayList<>(ohlcvs);
        Collections.reverse(series);

        OhlcvType ohlcvType = null;
        LocalDateTime dateTime = null;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal highPrice = BigDecimal.ZERO;
        BigDecimal lowPrice = BigDecimal.ZERO;
        BigDecimal closePrice = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;

        for(int i = 0; i < series.size(); i ++ ) {
            TradeAssetOhlcv ohlcv = series.get(i);
            if(i == 0) {
                ohlcvType = ohlcv.getOhlcvType();
                dateTime = ohlcv.getDateTime();
                openPrice = ohlcv.getOpenPrice();
                highPrice = ohlcv.getHighPrice();
                lowPrice = ohlcv.getLowPrice();
                closePrice = ohlcv.getClosePrice();
                volume = ohlcv.getVolume();
            }else{
                dateTime = ohlcv.getDateTime();
                if(ohlcv.getHighPrice().compareTo(highPrice) > 0) {
                    highPrice = ohlcv.getHighPrice();
                }
                if(ohlcv.getLowPrice().compareTo(lowPrice) < 0) {
                    lowPrice = ohlcv.getLowPrice();
                }
                closePrice = ohlcv.getClosePrice();
                volume = volume.add(ohlcv.getVolume());
            }
        }

        return TradeAssetOhlcv.builder()
                .ohlcvType(ohlcvType)
                .dateTime(dateTime)
                .openPrice(openPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .closePrice(closePrice)
                .volume(volume)
                .build();
    }

    public <C extends CalculateContext, R extends CalculateResult> List<R> calculate(OhlcvType ohlcvType, int period, C context) {
        List<TradeAssetOhlcv> ohlcvs = resample(ohlcvType, period);
        Collections.reverse(ohlcvs);

        // calculate
        Calculator<C,R> calculator = CalculatorFactory.getCalculator(context);
        List<R> calculateResults =  calculator.calculate(ohlcvs);

        // reverse and return
        Collections.reverse(calculateResults);
        return calculateResults;
    }

}