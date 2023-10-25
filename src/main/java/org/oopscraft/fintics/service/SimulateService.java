package org.oopscraft.fintics.service;

import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.thread.TradeAssetDecider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SimulateService {

    public Simulate simulate(Simulate simulate) {

        List<Ohlcv> ohlcvs = simulate.getOhlcvs();
        Double feeRate = simulate.getFeeRate();
        Double bidAskSpread = simulate.getBidAskSpread();
        List<Boolean> holdConditionResults = new ArrayList<>();

        int tradeCount = 0;
        Boolean previousHoldConditionResult = false;
        boolean hold = false;
        double buyPrice = 0;
        double profit = 0;

        for(int i = 0; i < ohlcvs.size(); i ++ ) {

            int toIndex = ohlcvs.size();
            int fromIndex = toIndex - i - 1;
            List<Ohlcv> currentOhlcvs = ohlcvs.subList(fromIndex, toIndex);

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
                    .asset(tradeAsset)
                    .minuteOhlcvs(currentOhlcvs)
                    .dailyOhlcvs(currentOhlcvs)
                    .build();

            boolean firstTrade = (i == 0);
            boolean lastTrade = (i == ohlcvs.size() - 1);

            TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                    .trade(trade)
                    .tradeAsset(tradeAsset)
                    .assetIndicator(assetIndicator)
                    .logger(log)
                    .firstTrade(firstTrade)
                    .lastTrade(lastTrade)
                    .build();
            Boolean holdConditionResult = tradeAssetDecider.execute();
            holdConditionResults.add(holdConditionResult);
            log.info("[{}] holdConditionResult: {}", i, holdConditionResult);

            if(holdConditionResult != null) {
                if (holdConditionResult) {
                    if (!hold) {
                        tradeCount ++;
                        log.info("=".repeat(80));
                        log.info("== buy[{}]", currentOhlcvs.get(0).getDateTime());
                        hold = true;
                        buyPrice = currentOhlcvs.get(0).getHighPrice() + bidAskSpread;
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
                        log.info("== sell[{}]", currentOhlcvs.get(0).getDateTime());
                        double sellPrice = currentOhlcvs.get(0).getLowPrice() - bidAskSpread;
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