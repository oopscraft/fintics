package org.oopscraft.fintics.trade;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.model.*;

import java.time.LocalDateTime;

@Builder
@RequiredArgsConstructor
public class TradeExecutor {

    private final IndiceClient indiceClient;

    private final TradeClient tradeClient;




//
//    @Getter
//    private final String tradeId;
//
//    @Getter
//    private final Integer interval;
//
//    @Getter
//    private final TradeLogAppender tradeLogAppender;
//
//    private final PlatformTransactionManager transactionManager;
//
//    private final AlarmService alarmService;
//
//    private final TradeRepository tradeRepository;
//
//    private final IndiceClient indiceClient;
//
//    private final IndiceOhlcvRepository indiceOhlcvRepository;
//
//    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;
//
//    private final OrderRepository orderRepository;
//
//    private final Logger log;
//
//    private Map<String,Boolean> holdConditionResultMap = new HashMap<>();
//
//    private Map<String,Integer> holdConditionResultCountMap = new HashMap<>();

    public void execute(Trade trade, LocalDateTime dateTime) throws InterruptedException {

        // client
        TradeClient tradeClient = TradeClientFactory.getClient(trade);


//        // check market opened
//        if(!tradeClient.isOpened()) {
//            log.info("Market not opened.");
//            return;
//        }
//
//        // checks start,end time
//        LocalDateTime dateTime = LocalDateTime.now();
//        if (!isOperatingTime(trade, dateTime.toLocalTime())) {
//            log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
//            return;
//        }

//        // logging
//        log.info("Check trade - [{}]", trade.getName());
//
//        // indice indicators
//        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
//        for(IndiceSymbol symbol : IndiceSymbol.values()) {
//            indiceClient.getMinuteOhlcvs(symbol);
//
//            // minute ohlcvs
//            List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol);
//            List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(symbol, minuteOhlcvs);
//            minuteOhlcvs.addAll(previousMinuteOhlcvs);
//
//            // daily ohlcvs
//            List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol);
//            List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(symbol, dailyOhlcvs);
//            dailyOhlcvs.addAll(previousDailyOhlcvs);
//
//            // add indicator
//            indiceIndicators.add(IndiceIndicator.builder()
//                    .symbol(symbol)
//                    .minuteOhlcvs(minuteOhlcvs)
//                    .dailyOhlcvs(dailyOhlcvs)
//                    .build());
//        }
//
//        // balance
//        Balance balance = tradeClient.getBalance();
//
//        // checks buy condition
//        for (TradeAsset tradeAsset : trade.getTradeAssets()) {
//            try {
//                Thread.sleep(100);
//
//                // check enabled
//                if (!tradeAsset.isEnabled()) {
//                    continue;
//                }
//
//                // logging
//                log.info("Check asset - [{}]", tradeAsset.getName());
//
//                // indicator
//                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
//                List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(tradeAsset, minuteOhlcvs);
//                minuteOhlcvs.addAll(previousMinuteOhlcvs);
//
//                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset);
//                List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(tradeAsset, dailyOhlcvs);
//                dailyOhlcvs.addAll(previousDailyOhlcvs);
//
//                AssetIndicator assetIndicator = AssetIndicator.builder()
//                        .symbol(tradeAsset.getSymbol())
//                        .name(tradeAsset.getName())
//                        .minuteOhlcvs(minuteOhlcvs)
//                        .dailyOhlcvs(dailyOhlcvs)
//                        .build();
//
//                // order book
//                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);
//
//                // executes trade asset decider
//                TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
//                        .holdCondition(trade.getHoldCondition())
//                        .logger(log)
//                        .dateTime(dateTime)
//                        .orderBook(orderBook)
//                        .balance(balance)
//                        .indiceIndicators(indiceIndicators)
//                        .assetIndicator(assetIndicator)
//                        .build();
//                Boolean holdConditionResult = tradeAssetDecider.execute();
//                log.info("holdConditionResult: {}", holdConditionResult);
//
//                // 0. checks threshold exceeded
//                int consecutiveCountOfHoldConditionResult = getConsecutiveCountOfHoldConditionResult(tradeAsset.getSymbol(), holdConditionResult);
//                log.info("consecutiveCountOfHoldConditionResult: {}", consecutiveCountOfHoldConditionResult);
//                if(consecutiveCountOfHoldConditionResult < trade.getThreshold()) {
//                    log.info("Threshold has not been exceeded yet - threshold is {}", trade.getThreshold());
//                    continue;
//                }
//
//                // 1. null is no operation
//                if (holdConditionResult == null) {
//                    continue;
//                }
//
//                // 2. buy and hold
//                if (holdConditionResult.equals(Boolean.TRUE)) {
//                    if (!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
//                        BigDecimal buyAmount = balance.getTotalAmount()
//                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
//                                .multiply(tradeAsset.getHoldRatio())
//                                .setScale(2, RoundingMode.HALF_UP);
//                        BigDecimal price = orderBook.getPrice();
//                        BigDecimal quantity = buyAmount
//                                .divide(price, MathContext.DECIMAL32);
//
//                        // buy
//                        log.info("Buy asset: {}", tradeAsset.getName());
//                        buyTradeAsset(trade, tradeAsset, trade.getOrderKind(), quantity, price);
//                    }
//                }
//
//                // 3. sell
//                else if (holdConditionResult.equals(Boolean.FALSE)) {
//                    if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
//                        // price, quantity
//                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol()).orElseThrow();
//                        BigDecimal price = orderBook.getPrice();
//                        BigDecimal quantity = balanceAsset.getOrderableQuantity();
//
//                        // sell
//                        log.info("Sell asset: {}", tradeAsset.getName());
//                        sellBalanceAsset(trade, balanceAsset, trade.getOrderKind(), quantity, price);
//                    }
//                }
//            } catch (Throwable e) {
//                log.error(e.getMessage(), e);
//                sendErrorAlarmIfEnabled(trade, tradeAsset, e);
//            }
//        }

    }

}
