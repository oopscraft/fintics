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

    private final OrderRepository orderRepository;

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

    public Page<Order> getOrders(String tradeId, OrderSearch orderSearch, Pageable pageable) {
        orderSearch.setTradeId(tradeId);
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .toList();
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
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

    public Optional<AssetIndicator> getTradeAssetIndicator(String tradeId, String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        Trade trade = getTrade(tradeId).orElseThrow();
        String tradeClientId = trade.getTradeClientId();
        TradeAsset tradeAsset = trade.getTradeAssets().stream()
                .filter(e -> Objects.equals(e.getAssetId(), assetId))
                .findFirst()
                .orElseThrow();

        // minute ohlcv
        LocalDateTime minuteDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(assetOhlcvRepository.findMaxDateTimeByTradeClientIdAndAssetIdAndOhlcvType(tradeClientId, assetId, OhlcvType.MINUTE)
                        .orElse(LocalDateTime.now()));
        LocalDateTime minuteDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(minuteDateTimeTo.minusDays(1));
        List<Ohlcv> minuteOhlcvs = assetOhlcvRepository.findAllByTradeClientIdAndAssetIdAndOhlcvType(tradeClientId, assetId, OhlcvType.MINUTE, minuteDateTimeFrom, minuteDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(assetOhlcvRepository.findMaxDateTimeByTradeClientIdAndAssetIdAndOhlcvType(tradeClientId, assetId, OhlcvType.DAILY)
                        .orElse(LocalDateTime.now()));
        LocalDateTime dailyDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(dailyDateTimeTo.minusMonths(1));
        List<Ohlcv> dailyOhlcvs = assetOhlcvRepository.findAllByTradeClientIdAndAssetIdAndOhlcvType(tradeClientId, assetId, OhlcvType.DAILY, dailyDateTimeFrom, dailyDateTimeTo, Pageable.unpaged())
                .stream()
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
