package org.oopscraft.fintics.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Trade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractTask {

    private final AssetRepository assetRepository;

    private final AssetMetaRepository assetMetaRepository;

    private final PlatformTransactionManager transactionManager;

    private final AssetClient assetClient;

    /**
     * initial run
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    public void onStartup() {
        collect();
    }

    /**
     * schedule collect
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void collect() {
        try {
            log.info("AssetCollector - Start collect asset.");
            saveAssets();
            saveAssetMetas();
            log.info("AssetCollector - End collect asset");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void saveAssets() {
        List<Asset> assets = assetClient.getAssets();
        List<AssetEntity> assetEntities = assets.stream()
                .map(asset -> AssetEntity.builder()
                        .assetId(asset.getAssetId())
                        .assetName(asset.getAssetName())
                        .market(asset.getMarket())
                        .exchange(asset.getExchange())
                        .type(asset.getType())
                        .marketCap(asset.getMarketCap())
                        .build())
                .collect(Collectors.toList());
        log.info("AssetCollector - save assetEntities:{}", assetEntities.size());
        saveEntities("assetEntities", assetEntities, transactionManager, assetRepository);
    }

    void saveAssetMetas() {
        List<AssetEntity> assetEntities = assetRepository.findAll();
        Instant dateTime = Instant.now();
        Instant expiredDateTime = dateTime.minusSeconds(60*24*7);
        for(AssetEntity assetEntity : assetEntities) {
            Asset asset = Asset.from(assetEntity);
            try {
                // check previous data
                List<AssetMetaEntity> previousAssetMetas = assetMetaRepository.findAllByAssetId(asset.getAssetId());
                if (previousAssetMetas.size() > 0) {
                    Instant previousDateTime = previousAssetMetas.get(0).getDateTime();
                    if (previousDateTime.isAfter(expiredDateTime)) {
                        continue;
                    }
                }

                // updates data
                List<AssetMeta> assetMetas = assetClient.getAssetMetas(asset);
                List<AssetMetaEntity> assetMetaEntities = new ArrayList<>();
                for (int i = 0; i < assetMetas.size(); i ++ ) {
                    AssetMeta assetMeta = assetMetas.get(i);
                    AssetMetaEntity assetMetaEntity = AssetMetaEntity.builder()
                            .assetId(assetMeta.getAssetId())
                            .name(assetMeta.getName())
                            .value(assetMeta.getValue())
                            .dateTime(dateTime)
                            .sort(i)
                            .build();
                    assetMetaEntities.add(assetMetaEntity);
                }
                saveEntities("assetMetaEntities", assetMetaEntities, transactionManager, assetMetaRepository);
                // force sleep
                Thread.sleep(1000);
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
    }

}
