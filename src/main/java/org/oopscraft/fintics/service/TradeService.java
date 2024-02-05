package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Trade;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final OrderRepository orderRepository;

    private final TradeClientFactory brokerClientFactory;

    @PersistenceContext
    private final EntityManager entityManager;

    public List<Trade> getTrades() {
        return tradeRepository.findAll(Sort.by(TradeEntity_.TRADE_NAME)).stream()
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
                                .enabled(tradeAsset.isEnabled())
                                .holdRatio(tradeAsset.getHoldRatio())
                                .build())
                .collect(Collectors.toList());
        tradeEntity.getTradeAssets().addAll(tradeAssetEntities);

        // save and return
        TradeEntity savedTradeEntity = tradeRepository.saveAndFlush(tradeEntity);
        entityManager.refresh(savedTradeEntity);
        return Trade.from(savedTradeEntity);
    }

    @Transactional
    public void deleteTrade(String tradeId) {
        tradeRepository.deleteById(tradeId);
        tradeRepository.flush();
    }

    public Page<Order> getOrders(String tradeId, String assetId, Order.Type type, Order.Result result, Pageable pageable) {
        // where
        Specification<OrderEntity> specification = Specification.where(null);
        specification = specification
                .and(OrderSpecifications.equalTradeId(tradeId))
                .and(Optional.ofNullable(assetId)
                        .map(OrderSpecifications::equalAssetId)
                        .orElse(null))
                .and(Optional.ofNullable(type)
                        .map(OrderSpecifications::equalType)
                        .orElse(null))
                .and(Optional.ofNullable(result)
                        .map(OrderSpecifications::equalResult)
                        .orElse(null));

        // sort
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(specification, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .toList();
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

    public Optional<Balance> getTradeBalance(String tradeId) throws InterruptedException {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getTradeClientId() != null) {
            TradeClient tradeClient = brokerClientFactory.getObject(trade.getTradeClientId(), trade.getTradeClientConfig());
            return Optional.ofNullable(tradeClient.getBalance());
        }else{
            return Optional.empty();
        }
    }

}
