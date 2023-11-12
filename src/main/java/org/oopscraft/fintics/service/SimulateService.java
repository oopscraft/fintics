package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.thread.TradeAssetDecider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulateService {

    private final IndiceService indiceService;

    public Simulate simulate(Simulate simulate) throws InterruptedException {

        LocalTime startAt = simulate.getStartAt();
        LocalTime endAt = simulate.getEndAt();
        List<Ohlcv> minuteOhlcvs = simulate.getMinuteOhlcvs();
        List<Ohlcv> dailyOhlcvs = simulate.getDailyOhlcvs();
        Double feeRate = simulate.getFeeRate();
        Double bidAskSpread = simulate.getBidAskSpread();
        List<Boolean> holdConditionResults = new ArrayList<>();

        int tradeCount = 0;
        boolean hold = false;
        double buyPrice = 0;
        double profit = 0;


        for(int i = minuteOhlcvs.size()-1; i >= 0; i --) {
            Ohlcv ohlcv = minuteOhlcvs.get(i);
            LocalDateTime dateTime = ohlcv.getDateTime();
            LocalTime time = dateTime.toLocalTime();
            log.info("dateTime:{}", dateTime);
            if(startAt != null && endAt != null) {
                if(time.isBefore(startAt) || time.isAfter(endAt)) {
                    continue;
                }
            }

            int fromIndex = i;
            int toIndex = minuteOhlcvs.size();
            List<Ohlcv> currentMinuteOhlcvs = minuteOhlcvs.subList(fromIndex, toIndex);

            Trade trade = Trade.builder()
                    .tradeId("simulate")
                    .holdCondition(simulate.getHoldCondition())
                    .interval(simulate.getInterval())
                    .build();

            TradeAsset tradeAsset = TradeAsset.builder()
                    .tradeId(trade.getTradeId())
                    .name("simulate TradeAsset")
                    .build();

            AssetIndicator assetIndicator = AssetIndicator.builder()
                    .symbol(tradeAsset.getSymbol())
                    .name(tradeAsset.getName())
                    .minuteOhlcvs(currentMinuteOhlcvs)
                    .dailyOhlcvs(dailyOhlcvs)
                    .build();

            List<IndiceIndicator> indiceIndicators = indiceService.getIndiceIndicators(dateTime);

            TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                    .holdCondition(simulate.getHoldCondition())
                    .logger(log)
                    .dateTime(dateTime)
                    .assetIndicator(assetIndicator)
                    .indiceIndicators(indiceIndicators)
                    .build();
            Boolean holdConditionResult = tradeAssetDecider.execute();
            holdConditionResults.add(holdConditionResult);
            log.info("[{}] holdConditionResult: {}", i, holdConditionResult);

            if(holdConditionResult != null) {
                if (holdConditionResult) {
                    if (!hold) {
                        tradeCount ++;
                        log.info("=".repeat(80));
                        log.info("== buy[{}]", currentMinuteOhlcvs.get(0).getDateTime());
                        hold = true;
                        buyPrice = currentMinuteOhlcvs.get(0).getClosePrice().doubleValue() + bidAskSpread;
                        log.info("== buyPrice:{}", buyPrice);
                        profit -= calculateFee(buyPrice, feeRate);
                        log.info("== profit:{}", profit);
                        log.info("=".repeat(80));
                    }
                }
                if (!holdConditionResult) {
                    if (hold) {
                        tradeCount ++;
                        log.info("=".repeat(80));
                        log.info("== sell[{}]", currentMinuteOhlcvs.get(0).getDateTime());
                        double sellPrice = currentMinuteOhlcvs.get(0).getClosePrice().doubleValue() - bidAskSpread;
                        log.info("== buyPrice:{}", buyPrice);
                        log.info("== sellPrice:{}", sellPrice);
                        profit -= calculateFee(sellPrice, feeRate);
                        profit += sellPrice - buyPrice;
                        log.info("== profit:{}", profit);
                        hold = false;
                        buyPrice = 0;
                        log.info("=".repeat(80));
                    }
                }
            }
        }

        log.info("#".repeat(80));
        log.info("== tradeCount:{}", tradeCount);
        log.info("== profit:{}", profit);
        log.info("#".repeat(80));

        simulate.setHoldConditionResults(holdConditionResults);
        return simulate;
    }

    private Double calculateFee(Double price, Double feeRate) {
        double fee = BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(feeRate).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        return fee;
    }

}
