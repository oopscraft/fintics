package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorContext;
import org.oopscraft.fintics.trade.order.OrderOperatorFactory;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TradeExecutor {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final BrokerAssetOhlcvRepository assetOhlcvRepository;

    private final OrderOperatorFactory orderOperatorFactory;

    @Setter
    private AlarmService alarmService;

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private final Map<String,Boolean> holdConditionResultMap = new HashMap<>();

    private final Map<String,Integer> holdConditionResultCountMap = new HashMap<>();

    @Builder
    private TradeExecutor(IndiceOhlcvRepository indiceOhlcvRepository, BrokerAssetOhlcvRepository assetOhlcvRepository, OrderOperatorFactory orderOperatorFactory) {
        this.indiceOhlcvRepository = indiceOhlcvRepository;
        this.assetOhlcvRepository = assetOhlcvRepository;
        this.orderOperatorFactory = orderOperatorFactory;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void execute(Trade trade, LocalDateTime dateTime, IndiceClient indiceClient, BrokerClient tradeClient) throws InterruptedException {
        log.info("Check trade - [{}]", trade.getTradeName());

        // check market opened
        if(!tradeClient.isOpened(dateTime)) {
            log.info("Market not opened.");
            return;
        }

        // checks start,end time
        if (!isOperatingTime(trade, dateTime)) {
            log.info("Not operating time - [{}] {} ~ {}", trade.getTradeName(), trade.getStartAt(), trade.getEndAt());
            return;
        }

        // indice indicators
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceId indiceId : IndiceId.values()) {
            // minute ohlcvs
            List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(indiceId, dateTime);
            List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(indiceId, minuteOhlcvs, dateTime);
            minuteOhlcvs.addAll(previousMinuteOhlcvs);

            // daily ohlcvs
            List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(indiceId, dateTime);
            List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(indiceId, dailyOhlcvs, dateTime);
            dailyOhlcvs.addAll(previousDailyOhlcvs);

            // add indicator
            indiceIndicators.add(IndiceIndicator.builder()
                    .indiceId(indiceId)
                    .minuteOhlcvs(minuteOhlcvs)
                    .dailyOhlcvs(dailyOhlcvs)
                    .build());
        }

        // balance
        Balance balance = tradeClient.getBalance();

        // checks buy condition
        for (TradeAsset tradeAsset : trade.getTradeAssets()) {
            try {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("Check asset - [{}]", tradeAsset.getAssetName());

                // indicator
                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(trade.getBrokerId(), tradeAsset.getAssetId(), minuteOhlcvs, dateTime);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(trade.getBrokerId(), tradeAsset.getAssetId(), dailyOhlcvs, dateTime);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                AssetIndicator assetIndicator = AssetIndicator.builder()
                        .assetId(tradeAsset.getAssetId())
                        .assetName(tradeAsset.getAssetName())
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();

                // order book
                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);

                // executes trade asset decider
                HoldConditionExecutor holdConditionExecutor = HoldConditionExecutor.builder()
                        .holdCondition(trade.getHoldCondition())
                        .dateTime(dateTime)
                        .orderBook(orderBook)
                        .balance(balance)
                        .indiceIndicators(indiceIndicators)
                        .assetIndicator(assetIndicator)
                        .build();
                holdConditionExecutor.setLog(log);
                Boolean holdConditionResult = holdConditionExecutor.execute();
                log.info("holdConditionResult: {}", holdConditionResult);

                // order operator
                OrderOperatorContext orderOperatorContext = OrderOperatorContext.builder()
                        .id(trade.getOrderOperatorId())
                        .tradeClient(tradeClient)
                        .trade(trade)
                        .balance(balance)
                        .orderBook(orderBook)
                        .build();
                OrderOperator orderOperator = orderOperatorFactory.getObject(orderOperatorContext);

                // 0. checks threshold exceeded
                int consecutiveCountOfHoldConditionResult = getConsecutiveCountOfHoldConditionResult(tradeAsset.getAssetId(), holdConditionResult);
                log.info("consecutiveCountOfHoldConditionResult: {}", consecutiveCountOfHoldConditionResult);
                if (consecutiveCountOfHoldConditionResult < trade.getThreshold()) {
                    log.info("Threshold has not been exceeded yet - threshold is {}", trade.getThreshold());
                    continue;
                }

                // 1. null is no operation
                if (holdConditionResult == null) {
                    continue;
                }

                // 2. buy asset
                if (holdConditionResult.equals(Boolean.TRUE)) {
                    orderOperator.buyTradeAsset(tradeAsset);
                }

                // 3. sell asset
                else if (holdConditionResult.equals(Boolean.FALSE)) {
                    orderOperator.sellTradeAsset(tradeAsset);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(trade, tradeAsset, e);
            }
        }
    }

    private boolean isOperatingTime(Trade trade, LocalDateTime dateTime) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getPreviousIndiceMinuteOhlcvs(IndiceId indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceOhlcvRepository.findAllByIndiceIdAndType(
                        indiceId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 1000)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousIndiceDailyOhlcvs(IndiceId indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceOhlcvRepository.findAllByIndiceIdAndType(
                        indiceId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 360)
                )
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetMinuteOhlcvs(String tradeClientId, String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetOhlcvRepository.findAllByBrokerIdAndAssetIdAndType(
                        tradeClientId,
                        assetId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 1000))
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetDailyOhlcvs(String tradeClientId, String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetOhlcvRepository.findAllByBrokerIdAndAssetIdAndType(
                        tradeClientId,
                        assetId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private int getConsecutiveCountOfHoldConditionResult(String assetId, Boolean holdConditionResult) {
        Boolean previousHoldConditionResult = holdConditionResultMap.get(assetId);
        int holdConditionResultCount = holdConditionResultCountMap.getOrDefault(assetId, 0);

        // increases match count
        if(Objects.equals(holdConditionResult, previousHoldConditionResult)) {
            holdConditionResultCount ++;
        }else{
            holdConditionResultCount = 1;
        }

        // store
        holdConditionResultMap.put(assetId, holdConditionResult);
        holdConditionResultCountMap.put(assetId, holdConditionResultCount);

        // return
        return holdConditionResultCount;
    }

    private void sendErrorAlarmIfEnabled(Trade trade, TradeAsset tradeAsset, Throwable t) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s]", trade.getTradeName(), tradeAsset != null ? tradeAsset.getAssetName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
