package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.AssetRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractCollector {

    private final TradeRepository tradeRepository;

    private final TradeClientFactory brokerClientFactory;

    private final AssetRepository assetRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 3600_000)
    @Transactional
    public void collect() {
        log.info("Start collect broker asset.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        List<String> completedTradeClientIds = new ArrayList<>();
        for (TradeEntity tradeEntity : tradeEntities) {
            try {
                String tradeClientId = tradeEntity.getTradeClientId();
                if(!completedTradeClientIds.contains(tradeClientId)) {
                    Trade trade = Trade.from(tradeEntity);
                    saveAssets(trade);
                    completedTradeClientIds.add(tradeClientId);
                }
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        log.info("End collect broker asset");
    }

    protected void saveAssets(Trade trade) {
        TradeClient tradeClient = brokerClientFactory.getObject(trade);
        List<AssetEntity> assetEntities = tradeClient.getAssets().stream()
                .map(brokerAsset -> AssetEntity.builder()
                        .assetId(brokerAsset.getAssetId())
                        .assetName(brokerAsset.getAssetName())
                        .type(brokerAsset.getType())
                        .marketCap(brokerAsset.getMarketCap())
                        .issuedShares(brokerAsset.getIssuedShares())
                        .per(brokerAsset.getPer())
                        .roe(brokerAsset.getRoe())
                        .roa(brokerAsset.getRoa())
                        .build())
                .collect(Collectors.toList());
        saveEntities(assetEntities, transactionManager, assetRepository);
    }

}
