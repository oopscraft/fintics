package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;

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
        TradeEntity tradeEntity = null;
        if(trade.getTradeId() != null) {
            tradeEntity = tradeRepository.findById(trade.getTradeId()).orElseThrow();
        }
        if(tradeEntity == null) {
            tradeEntity = TradeEntity.builder()
                    .tradeId(IdGenerator.uuid())
                    .build();
        }
        tradeEntity.setName(trade.getName());
        tradeEntity.setEnabled(trade.isEnabled());
        tradeEntity.setInterval(trade.getInterval());
        tradeEntity.setClientType(trade.getClientType());
        tradeEntity.setClientProperties(trade.getClientProperties());
        tradeEntity.setBuyRule(trade.getBuyRule());
        tradeEntity.setSellRule(trade.getSellRule());

        // trade asset
        tradeEntity.getTradeAssetEntities().clear();
        List<TradeAssetEntity> tradeAssetEntities = trade.getTradeAssets().stream()
                .map(tradeAsset ->
                        TradeAssetEntity.builder()
                                .tradeId(tradeAsset.getTradeId())
                                .symbol(tradeAsset.getSymbol())
                                .name(tradeAsset.getName())
                                .type(tradeAsset.getType())
                                .enabled(tradeAsset.isEnabled())
                                .tradeRatio(tradeAsset.getTradeRatio())
                                .limitRatio(tradeAsset.getLimitRatio())
                                .build())
                .collect(Collectors.toList());
        tradeEntity.getTradeAssetEntities().addAll(tradeAssetEntities);

        tradeEntity = tradeRepository.saveAndFlush(tradeEntity);
        return Trade.from(tradeEntity);
    }

}
