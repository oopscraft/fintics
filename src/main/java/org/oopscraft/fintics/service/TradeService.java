package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private static final String CACHE_TRADE_ASSET_INDICATOR = "TradeService.getTradeAssetIndicator";

    private final TradeRepository tradeRepository;

    private final AlarmService alarmService;

    public List<Trade> getTrades() {
        return tradeRepository.findAll().stream()
                .map(Trade::from)
                .collect(Collectors.toList());
    }

    public Optional<Trade> getTrade(String tradeId) {
        return tradeRepository.findById(tradeId)
                .map(Trade::from);
    }

    @Transactional
    public Trade saveTrade(Trade trade) {
        final TradeEntity tradeEntity;
        if(trade.getTradeId() != null) {
            tradeEntity = tradeRepository.findById(trade.getTradeId()).orElseThrow();
        } else {
            tradeEntity = TradeEntity.builder()
                    .tradeId(IdGenerator.uuid())
                    .userId(trade.getUserId())
                    .build();
        }
        tradeEntity.setName(trade.getName());
        tradeEntity.setEnabled(trade.isEnabled());
        tradeEntity.setInterval(trade.getInterval());
        tradeEntity.setStartAt(trade.getStartAt());
        tradeEntity.setEndAt(trade.getEndAt());
        tradeEntity.setClientType(trade.getClientType());
        if(trade.getClientProperties() != null) {
            String clientProperties = PbePropertiesUtil.encode(trade.getClientProperties());
            tradeEntity.setClientProperties(clientProperties);
        }
        tradeEntity.setHoldCondition(trade.getHoldCondition());
        tradeEntity.setAlarmId(trade.getAlarmId());
        tradeEntity.setAlarmOnError(trade.isAlarmOnError());
        tradeEntity.setAlarmOnOrder(trade.isAlarmOnOrder());
        tradeEntity.setPublicEnabled(trade.isPublicEnabled());

        // trade asset
        tradeEntity.getTradeAssetEntities().clear();
        List<TradeAssetEntity> tradeAssetEntities = trade.getTradeAssets().stream()
                .map(tradeAsset ->
                        TradeAssetEntity.builder()
                                .tradeId(tradeEntity.getTradeId())
                                .symbol(tradeAsset.getSymbol())
                                .name(tradeAsset.getName())
                                .type(tradeAsset.getType())
                                .enabled(tradeAsset.isEnabled())
                                .holdRatio(tradeAsset.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        tradeEntity.getTradeAssetEntities().addAll(tradeAssetEntities);

        // save and return
        TradeEntity savedTradeEntity = tradeRepository.saveAndFlush(tradeEntity);
        return Trade.from(savedTradeEntity);
    }

    @Transactional
    public void deleteTrade(String tradeId) {
        tradeRepository.deleteById(tradeId);
        tradeRepository.flush();
    }

    public Optional<Balance> getTradeBalance(String tradeId) {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getClientType() != null) {
            TradeClient tradeClient = TradeClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
            return Optional.ofNullable(tradeClient.getBalance());
        }else{
            return Optional.empty();
        }
    }

    @Cacheable(value = CACHE_TRADE_ASSET_INDICATOR, key = "#tradeId + ':' + #symbol")
    public synchronized Optional<AssetIndicator> getTradeAssetIndicator(String tradeId, String symbol) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAssets().stream()
                .filter(e -> Objects.equals(e.getSymbol(), symbol))
                .findFirst()
                .orElseThrow();

        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);
        List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
        List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset);
        return Optional.ofNullable(AssetIndicator.builder()
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
                .orderBook(orderBook)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

    @CacheEvict(value = CACHE_TRADE_ASSET_INDICATOR, allEntries = true)
    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public synchronized void purgeTradeAssetIndicatorCache() {
        log.info("cacheEvict[{}]", CACHE_TRADE_ASSET_INDICATOR);
    }

    public void buyTradeAsset(String tradeId, String symbol, Integer quantity) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAsset(symbol).orElseThrow();
        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        tradeClient.buyAsset(tradeAsset, quantity);
        if (trade.isAlarmOnOrder()) {
            if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                String subject = String.format("[%s]", trade.getName());
                String content = String.format("[%s] Buy %d", tradeAsset.getName(), quantity);
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    public void sellBalanceAsset(String tradeId, String symbol, Integer quantity) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        Balance balance = tradeClient.getBalance();
        BalanceAsset balanceAsset = balance.getBalanceAsset(symbol).orElseThrow();
        tradeClient.sellAsset(balanceAsset, quantity);
        if (trade.isAlarmOnOrder()) {
            if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                String subject = String.format("[%s]", trade.getName());
                String content = String.format("[%s] Sell %d", balanceAsset.getName(), quantity);
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    @Transactional
    public void sendErrorAlarmIfEnabled(String tradeId, Throwable t) {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s]", trade.getName());
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
