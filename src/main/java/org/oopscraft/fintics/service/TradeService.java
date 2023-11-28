package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    private final OrderRepository orderRepository;

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

        // trade asset
        tradeEntity.getTradeAssetEntities().clear();
        List<TradeAssetEntity> tradeAssetEntities = trade.getTradeAssets().stream()
                .map(tradeAsset ->
                        TradeAssetEntity.builder()
                                .tradeId(tradeEntity.getTradeId())
                                .symbol(tradeAsset.getSymbol())
                                .name(tradeAsset.getName())
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

    public Optional<TradeAssetIndicator> getTradeAssetIndicator(String tradeId, String symbol) {
        Trade trade = getTrade(tradeId).orElseThrow();
        TradeAsset tradeAsset = trade.getTradeAssets().stream()
                .filter(e -> Objects.equals(e.getSymbol(), symbol))
                .findFirst()
                .orElseThrow();

        LocalDateTime minuteMaxDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeId, symbol, OhlcvType.MINUTE)
                .orElse(LocalDateTime.now());
        List<Ohlcv> minuteOhlcvs = tradeAssetOhlcvRepository.findAllBySymbolAndOhlcvType(tradeId, symbol, OhlcvType.MINUTE, minuteMaxDateTime.minusDays(1), minuteMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        LocalDateTime dailyMaxDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeId, symbol, OhlcvType.DAILY)
                .orElse(LocalDateTime.now());
        List<Ohlcv> dailyOhlcvs = tradeAssetOhlcvRepository.findAllBySymbolAndOhlcvType(tradeId, symbol, OhlcvType.DAILY, dailyMaxDateTime.minusMonths(1), dailyMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        return Optional.ofNullable(TradeAssetIndicator.builder()
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

}
