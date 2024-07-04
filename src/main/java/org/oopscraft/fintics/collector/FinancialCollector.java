package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.financial.FinancialClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Financial;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialCollector extends AbstractCollector {

    private final AssetRepository assetRepository;

    private final FinancialRepository assetFinancialRepository;

    private final FinancialClient financialClient;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 600_000, fixedDelay = Long.MAX_VALUE)
    public void onStartup() {
        collect();
    }

    @Scheduled(cron = "0 0 19 * * *")
    public void collect() {
        try {
            log.info("AssetFinancialCollector - Start collect asset financial.");
            List<AssetEntity> assetEntities = assetRepository.findAll();
            for (AssetEntity assetEntity : assetEntities) {
                try {
                    Asset asset = Asset.from(assetEntity);
                    Financial assetFinancial = financialClient.getAssetFinancial(asset);
                    FinancialEntity assetFinancialEntity = FinancialEntity.builder()
                            .assetId(assetEntity.getAssetId())
                            .dateTime(Instant.now())
                            .issuedShares(assetFinancial.getIssuedShares())
                            .totalAssets(assetFinancial.getTotalAssets())
                            .totalEquity(assetFinancial.getTotalEquity())
                            .netIncome(assetFinancial.getNetIncome())
                            .eps(assetFinancial.getEps())
                            .per(assetFinancial.getPer())
                            .roe(assetFinancial.getRoe())
                            .roa(assetFinancial.getRoa())
                            .ebitda(assetFinancial.getEbitda())
                            .dividendYield(assetFinancial.getDividendYield())
                            .build();
                    String unitName = String.format("assetFinancialEntity[%s]: %s", asset.getAssetName(), assetFinancialEntity);
                    saveEntities(unitName, List.of(assetFinancialEntity), transactionManager, assetFinancialRepository);

                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("AssetCollector - End collect asset financial.");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
