package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.AssetRepository;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector {

    @Value("${fintics.collector.asset-collector.limit:100}")
    private Integer limit = 100;

    private final AssetClient assetClient;

    private final AssetRepository assetRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 3_000, fixedDelay = 360_000 * 24)
    @Transactional
    public void collectAssets() throws InterruptedException {
        log.info("Start collect assets.");
        LocalDateTime collectedAt = LocalDateTime.now();
        BigDecimal dividedBy = BigDecimal.valueOf(2);
        int etfLimit = BigDecimal.valueOf(limit).divide(dividedBy).setScale(0, RoundingMode.CEILING).intValue();
        int stockLimit = BigDecimal.valueOf(limit).divide(dividedBy).setScale(0, RoundingMode.FLOOR).intValue();

        // stock
        collectStockAssets(stockLimit, collectedAt);

        // etf
        collectEtfAssets(etfLimit, collectedAt);

        // delete not merged
        entityManager.createQuery(
                        "delete from AssetEntity where collectedAt <> :collectedAt"
                )
                .setParameter("collectedAt", collectedAt)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        log.info("End collect assets.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void collectStockAssets(int limit, LocalDateTime collectedAt) throws InterruptedException {
        List<Asset> stockAssets = assetClient.getStockAssets(0, limit);
        List<AssetEntity> stockAssetEntities = stockAssets.stream()
                .map(asset -> AssetEntity.builder()
                        .symbol(asset.getSymbol())
                        .name(asset.getName())
                        .type(AssetType.STOCK)
                        .collectedAt(collectedAt)
                        .build())
                .collect(Collectors.toList());
        assetRepository.saveAllAndFlush(stockAssetEntities);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void collectEtfAssets(int limit, LocalDateTime collectedAt) throws InterruptedException {
        List<Asset> etfAssets = assetClient.getEtfAssets(0, limit);
        List<AssetEntity> etfAssetEntities = etfAssets.stream()
                .map(asset -> AssetEntity.builder()
                        .symbol(asset.getSymbol())
                        .name(asset.getName())
                        .type(AssetType.ETF)
                        .collectedAt(collectedAt)
                        .build())
                .collect(Collectors.toList());
        assetRepository.saveAllAndFlush(etfAssetEntities);
    }


}
