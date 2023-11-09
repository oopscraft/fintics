package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.arch4j.core.user.User;
import org.oopscraft.arch4j.core.user.dao.UserEntity;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
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

    private final OrderRepository orderRepository;

    private final AlarmService alarmService;

    private final PlatformTransactionManager transactionManager;

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

    public Optional<Balance> getTradeBalance(String tradeId) throws InterruptedException {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getClientType() != null) {
            TradeClient tradeClient = TradeClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
            return Optional.ofNullable(tradeClient.getBalance());
        }else{
            return Optional.empty();
        }
    }

    @Cacheable(value = CACHE_TRADE_ASSET_INDICATOR, key = "#tradeId + ':' + #symbol")
    public Optional<AssetIndicator> getTradeAssetIndicator(String tradeId, String symbol) throws InterruptedException {
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
    public void purgeTradeAssetIndicatorCache() {
        log.info("cacheEvict[{}]", CACHE_TRADE_ASSET_INDICATOR);
    }

    @Transactional
    public void buyTradeAsset(String tradeId, String symbol, String name, Integer quantity) throws InterruptedException {
        OrderResult orderResult = null;
        String errorMessage = null;
        try {
            Trade trade = getTrade(tradeId).orElseThrow();
            TradeAsset tradeAsset = trade.getTradeAsset(symbol).orElseThrow();
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            tradeClient.buyAsset(tradeAsset, quantity);

            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Buy %d", tradeAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }

            orderResult = OrderResult.COMPLETED;
        } catch(Throwable e) {
            orderResult = OrderResult.FAILED;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            saveTradeOrder(OrderType.BUY, tradeId, symbol, name, quantity, orderResult, errorMessage);
        }


    }

    @Transactional
    public void sellBalanceAsset(String tradeId, String symbol, String name, Integer quantity) throws InterruptedException {
        OrderResult orderResult = null;
        String errorMessage = null;
        try {
            Trade trade = getTrade(tradeId).orElseThrow();
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            Balance balance = tradeClient.getBalance();
            BalanceAsset balanceAsset = balance.getBalanceAsset(symbol).orElseThrow();
            tradeClient.sellAsset(balanceAsset, quantity);

            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Sell %d", balanceAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }

            orderResult = OrderResult.COMPLETED;
        } catch(Throwable e) {
            orderResult = OrderResult.FAILED;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            saveTradeOrder(OrderType.SELL, tradeId, symbol, name, quantity, orderResult, errorMessage);
        }
    }

    @Transactional
    public void sendErrorAlarmIfEnabled(String tradeId, Throwable t) throws InterruptedException {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s]", trade.getName());
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    private void saveTradeOrder(OrderType orderType, String tradeId, String symbol, String name, Integer quantity, OrderResult orderResult, String errorMessage) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            orderRepository.saveAndFlush(OrderEntity.builder()
                    .orderId(IdGenerator.uuid())
                    .orderAt(LocalDateTime.now())
                    .orderType(orderType)
                    .tradeId(tradeId)
                    .symbol(symbol)
                    .name(name)
                    .quantity(quantity)
                    .orderResult(orderResult)
                    .errorMessage(errorMessage)
                    .build());
        });
    }

    public Page<Order> getTradeOrders(String tradeId, Pageable pageable) {
        Page<OrderEntity> orderEntityPage = orderRepository.findAllByTradeId(tradeId, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .collect(Collectors.toList());
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

}
