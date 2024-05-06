package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.model.Trade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractCollector {

    private final TradeRepository tradeRepository;

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory brokerClientFactory;

    private final AssetRepository assetRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    public void onStartup() {
        collect();
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void collect() {
        try {
            log.info("AssetCollector - Start collect broker asset.");
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            List<String> completedBrokerIds = new ArrayList<>();
            for (TradeEntity tradeEntity : tradeEntities) {
                try {
                    String brokerId = tradeEntity.getBrokerId();
                    if (!completedBrokerIds.contains(brokerId)) {
                        Trade trade = Trade.from(tradeEntity);
                        saveAssets(trade);
                        completedBrokerIds.add(brokerId);
                    }
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("AssetCollector - End collect broker asset");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected void saveAssets(Trade trade) {
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        List<AssetEntity> assetEntities = brokerClient.getAssets().stream()
                .map(asset -> AssetEntity.builder()
                        .assetId(asset.getAssetId())
                        .assetName(asset.getAssetName())
                        .market(asset.getMarket())
                        .exchange(asset.getExchange())
                        .type(asset.getType())
                        .marketCap(asset.getMarketCap())
                        .issuedShares(asset.getIssuedShares())
                        .per(asset.getPer())
                        .roe(asset.getRoe())
                        .roa(asset.getRoa())
                        .build())
                .collect(Collectors.toList());
        log.info("AssetCollector - save assetEntities:{}", assetEntities.size());
        saveEntities("assetEntities", assetEntities, transactionManager, assetRepository);
    }

}
