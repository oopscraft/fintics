package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Asset;
import org.springframework.data.domain.Sort;
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
public class AssetCollector extends AbstractCollector {

    private final AssetRepository assetRepository;

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
            updateAssetDetail();
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
                        .name(asset.getName())
                        .market(asset.getMarket())
                        .exchange(asset.getExchange())
                        .type(asset.getType())
                        .marketCap(asset.getMarketCap())
                        .build())
                .collect(Collectors.toList());
        log.info("AssetCollector - save assetEntities:{}", assetEntities.size());
        saveEntities("assetEntities", assetEntities, transactionManager, assetRepository);
    }

    void updateAssetDetail() {
        List<AssetEntity> assetEntities = assetRepository.findAll(Sort.by(AssetEntity_.MARKET_CAP).descending());
        for(AssetEntity assetEntity : assetEntities) {
            Asset asset = Asset.from(assetEntity);
            try {
                assetClient.applyAssetDetail(asset);
                assetEntity.setPer(asset.getPer());
                assetEntity.setEps(asset.getEps());
                assetEntity.setRoe(asset.getRoe());
                assetEntity.setRoa(asset.getRoa());
                assetEntity.setDividendYield(asset.getDividendYield());
                assetRepository.saveAndFlush(assetEntity);
                // force sleep
                Thread.sleep(1000);
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
    }

}
