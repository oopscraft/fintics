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
public class AssetCollector {

    private final TradeRepository tradeRepository;

    private final TradeClientFactory brokerClientFactory;

    private final AssetRepository brokerAssetRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 3600_000)
    @Transactional
    public void collect() {
        log.info("Start collect broker asset.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        List<String> processedBrokerIds = new ArrayList<>();
        for (TradeEntity tradeEntity : tradeEntities) {
            try {
                String brokerId = tradeEntity.getTradeClientId();
                if(!processedBrokerIds.contains(brokerId)) {
                    Trade trade = Trade.from(tradeEntity);
                    saveBrokerAssets(trade);
                    processedBrokerIds.add(brokerId);
                }
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        log.info("End collect broker asset");
    }

    protected void saveBrokerAssets(Trade trade) {
        TradeClient brokerClient = brokerClientFactory.getObject(trade);
        List<AssetEntity> brokerAssetEntities = brokerClient.getAssets().stream()
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

        // save
        TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            int count = 0;
            for(AssetEntity brokerAssetEntity : brokerAssetEntities) {
                count ++;
                brokerAssetRepository.saveAndFlush(brokerAssetEntity);

                // middle commit
                if(count % 100 == 0) {
                    transactionManager.commit(status);
                    status = transactionManager.getTransaction(definition);
                }
            }

            // final commit
            transactionManager.commit(status);

        } catch(Exception e) {
            transactionManager.rollback(status);
        } finally {
            if(!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

}
