package org.oopscraft.fintics.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.simulate.SimulateIndiceClient;
import org.oopscraft.fintics.simulate.SimulateLogAppender;
import org.oopscraft.fintics.simulate.SimulateRunnable;
import org.oopscraft.fintics.simulate.SimulateTradeClient;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    public List<Trade> getTrades() {
        return tradeRepository.findAllOrderByName().stream()
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
                    .build();
        }
        tradeEntity.setTradeName(trade.getTradeName());
        tradeEntity.setEnabled(trade.isEnabled());
        tradeEntity.setInterval(trade.getInterval());
        tradeEntity.setThreshold(trade.getThreshold());
        tradeEntity.setStartAt(trade.getStartAt());
        tradeEntity.setEndAt(trade.getEndAt());
        tradeEntity.setTradeClientId(trade.getTradeClientId());
        if(trade.getTradeClientConfig() != null) {
            String clientProperties = PbePropertiesUtil.encode(trade.getTradeClientConfig());
            tradeEntity.setTradeClientConfig(clientProperties);
        }
        tradeEntity.setHoldCondition(trade.getHoldCondition());
        tradeEntity.setOrderOperatorId(trade.getOrderOperatorId());
        tradeEntity.setOrderKind(trade.getOrderKind());
        tradeEntity.setAlarmId(trade.getAlarmId());
        tradeEntity.setAlarmOnError(trade.isAlarmOnError());
        tradeEntity.setAlarmOnOrder(trade.isAlarmOnOrder());

        // trade asset
        tradeEntity.getTradeAssets().clear();
        List<TradeAssetEntity> tradeAssetEntities = trade.getTradeAssets().stream()
                .map(tradeAsset ->
                        TradeAssetEntity.builder()
                                .tradeId(tradeEntity.getTradeId())
                                .assetId(tradeAsset.getAssetId())
                                .assetName(tradeAsset.getAssetName())
                                .enabled(tradeAsset.isEnabled())
                                .holdRatio(tradeAsset.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        tradeEntity.getTradeAssets().addAll(tradeAssetEntities);

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
        if(trade.getTradeClientId() != null) {
            TradeClient tradeClient = TradeClientFactory.getClient(trade.getTradeClientId(), trade.getTradeClientConfig());
            return Optional.ofNullable(tradeClient.getBalance());
        }else{
            return Optional.empty();
        }
    }

    public Optional<AssetIndicator> getTradeAssetIndicator(String tradeId, String assetId) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAssets().stream()
                .filter(e -> Objects.equals(e.getAssetId(), assetId))
                .findFirst()
                .orElseThrow();

        LocalDateTime minuteMaxDateTime = assetOhlcvRepository.findMaxDateTimeByTradeClientIdAndAssetIdAndOhlcvType(trade.getTradeClientId(), assetId, OhlcvType.MINUTE)
                .orElse(LocalDateTime.now());
        List<Ohlcv> minuteOhlcvs = assetOhlcvRepository.findAllByTradeClientIdAndAssetIdAndOhlcvType(trade.getTradeClientId(), assetId, OhlcvType.MINUTE, minuteMaxDateTime.minusDays(1), minuteMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        LocalDateTime dailyMaxDateTime = assetOhlcvRepository.findMaxDateTimeByTradeClientIdAndAssetIdAndOhlcvType(trade.getTradeClientId(), assetId, OhlcvType.DAILY)
                .orElse(LocalDateTime.now());
        List<Ohlcv> dailyOhlcvs = assetOhlcvRepository.findAllByTradeClientIdAndAssetIdAndOhlcvType(trade.getTradeClientId(), assetId, OhlcvType.DAILY, dailyMaxDateTime.minusMonths(1), dailyMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        return Optional.ofNullable(AssetIndicator.builder()
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

}
