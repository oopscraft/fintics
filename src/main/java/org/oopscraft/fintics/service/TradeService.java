package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.arch4j.core.security.SecurityUtils;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.thread.TradeThreadManager;
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
            Client client = ClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
            return Optional.ofNullable(client.getBalance());
        }else{
            return Optional.empty();
        }
    }

    @Cacheable(value = "TradeService.getTradeAssetIndicator", key = "#symbol")
    public Optional<AssetIndicator> getTradeAssetIndicator(String tradeId, String symbol) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAssets().stream()
                .filter(e -> Objects.equals(e.getSymbol(), symbol))
                .findFirst()
                .orElseThrow();

        Client client = ClientFactory.getClient(trade);
        OrderBook orderBook = client.getOrderBook(tradeAsset);
        List<Ohlcv> minuteOhlcvs = client.getMinuteOhlcvs(tradeAsset);
        List<Ohlcv> dailyOhlcvs = client.getDailyOhlcvs(tradeAsset);
        return Optional.ofNullable(AssetIndicator.builder()
                .asset(tradeAsset)
                .orderBook(orderBook)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

    @CacheEvict(value = "TradeService.getTradeAssetIndicator", allEntries = true)
    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void purgeTransactionIndicatorCache() {
        log.info("cacheEvict[TradeService.getTradeAssetIndicator");
    }

    public void buyTradeAsset(String tradeId, String symbol, Integer quantity) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAsset(symbol).orElseThrow();
        Client client = ClientFactory.getClient(trade);
        client.buyAsset(tradeAsset, quantity);
        sendOrderAlarmIfEnabled(trade, String.format("[%s] Buy %d", tradeAsset.getName(), quantity));
    }

    public void sellBalanceAsset(String tradeId, String symbol, Integer quantity) {
        Trade trade = getTrade(tradeId).orElseThrow();
        Client client = ClientFactory.getClient(trade);
        Balance balance = client.getBalance();
        BalanceAsset balanceAsset = balance.getBalanceAsset(symbol).orElseThrow();
        client.sellAsset(balanceAsset, quantity);
        sendOrderAlarmIfEnabled(trade, String.format("[%s] Sell %d", balanceAsset.getName(), quantity));
    }

    private void sendOrderAlarmIfEnabled(Trade trade, String content) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnOrder()) {
                String subject = String.format("[%s]", trade.getName());
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
