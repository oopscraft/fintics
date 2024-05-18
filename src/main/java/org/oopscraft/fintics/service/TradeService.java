package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final OrderRepository orderRepository;

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final AssetRepository assetRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    private final SimulateRepository simulateRepository;

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
        tradeEntity.setBrokerId(trade.getBrokerId());
        tradeEntity.setStrategyId(trade.getStrategyId());
        tradeEntity.setStrategyVariables(trade.getStrategyVariables());
        tradeEntity.setOrderKind(trade.getOrderKind());
        tradeEntity.setAlarmId(trade.getAlarmId());
        tradeEntity.setAlarmOnError(trade.isAlarmOnError());
        tradeEntity.setAlarmOnOrder(trade.isAlarmOnOrder());

        // trade asset
        tradeEntity.getTradeAssets().clear();
        AtomicInteger sort = new AtomicInteger(0);
        List<TradeAssetEntity> tradeAssetEntities = trade.getTradeAssets().stream()
                .map(tradeAsset ->
                        TradeAssetEntity.builder()
                                .tradeId(tradeEntity.getTradeId())
                                .assetId(tradeAsset.getAssetId())
                                .sort(sort.getAndIncrement())
                                .enabled(tradeAsset.isEnabled())
                                .holdingWeight(tradeAsset.getHoldingWeight())
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

    public Optional<Balance> getBalance(String tradeId) throws InterruptedException {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getBrokerId() != null) {
            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Balance balance = brokerClient.getBalance();
            balance.getBalanceAssets().forEach(balanceAsset -> {
                assetRepository.findById(balanceAsset.getAssetId()).ifPresent(assetEntity -> {
                    balanceAsset.setType(assetEntity.getType());
                    balanceAsset.setIssuedShares(assetEntity.getIssuedShares());
                    balanceAsset.setMarketCap(assetEntity.getMarketCap());
                    balanceAsset.setPer(assetEntity.getPer());
                    balanceAsset.setRoe(assetEntity.getRoe());
                    balanceAsset.setRoa(assetEntity.getRoa());
                });
            });
            return Optional.of(balance);
        }else{
            return Optional.empty();
        }
    }

    public Page<Simulate> getSimulates(String tradeId, Simulate.Status status, Boolean favorite, Pageable pageable) {
        // where
        Specification<SimulateEntity> specification = Specification.where(null);
        specification = specification
                .and(SimulateSpecifications.equalTradeId(tradeId))
                .and(Optional.ofNullable(status)
                        .map(SimulateSpecifications::equalStatus)
                        .orElse(null))
                .and(Optional.ofNullable(favorite)
                        .map(SimulateSpecifications::equalFavorite)
                        .orElse(null));
        // sort
        Sort sort = Sort.by(SimulateEntity_.STARTED_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        // find
        Page<SimulateEntity> simulateEntityPage = simulateRepository.findAll(specification, pageable);
        List<Simulate> simulates = simulateEntityPage.getContent().stream()
                .map(Simulate::from)
                .toList();
        long total = simulateEntityPage.getTotalElements();
        return new PageImpl<>(simulates, pageable, total);
    }

}
