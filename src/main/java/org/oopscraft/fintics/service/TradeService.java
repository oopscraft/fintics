package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.arch4j.core.security.SecurityUtils;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.thread.TradeThreadManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;

    private final TradeThreadManager tradeThreadManager;

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

}
