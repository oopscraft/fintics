package org.oopscraft.fintics.basket;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.BasketAssetEntity;
import org.oopscraft.fintics.dao.BasketAssetRepository;
import org.oopscraft.fintics.dao.BasketRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.TradeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Builder
@Slf4j
public class BasketRebalanceTask {

    private final Basket basket;

    private final BasketRepository basketRepository;

    private final BasketAssetRepository basketAssetRepository;

    private final TradeService tradeService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    public void execute() {
        BasketScriptRunner basketRebalance = basketScriptRunnerFactory.getObject(basket);
        List<BasketRebalanceResult> basketChanges = basketRebalance.run();
        log.info("basketChanges: {}", basketChanges);

        // clears holdWeights
        for (BasketAsset basketAsset : basket.getBasketAssets()) {
            if (!basketAsset.isFixed()) {
                BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                        .basketId(basketAsset.getBasketId())
                        .assetId(basketAsset.getAssetId())
                        .build();
                BasketAssetEntity basketAssetEntity = basketAssetRepository.findById(pk).orElseThrow();
                basketAssetEntity.setHoldingWeight(BigDecimal.ZERO);
                basketAssetRepository.saveAndFlush(basketAssetEntity);
            }
        }

        // adds basket assets
        int sort = basket.getBasketAssets().stream()
                .mapToInt(BasketAsset::getSort)
                .max()
                .orElse(0) + 1;
        for (BasketRebalanceResult basketChange : basketChanges) {
            String market = basket.getMarket();
            String symbol = basketChange.getSymbol();
            String assetId = String.format("%s.%s", market, symbol);
            BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                    .basketId(basket.getBasketId())
                    .assetId(assetId)
                    .build();
            BasketAssetEntity basketAssetEntity = basketAssetRepository.findById(pk).orElse(null);
            if (basketAssetEntity == null) {
                basketAssetEntity = BasketAssetEntity.builder()
                        .basketId(basket.getBasketId())
                        .assetId(assetId)
                        .sort(sort++)
                        .enabled(true)
                        .build();
            }
            basketAssetEntity.setHoldingWeight(basketChange.getHoldingWeight());
            basketAssetRepository.saveAndFlush(basketAssetEntity);
        }

        // clear holdWeight zero + not holding
        List<Trade> trades = tradeService.getTrades().stream()
                .filter(trade -> Objects.equals(trade.getBasketId(), basket.getBasketId()))
                .toList();
        // finds all relative balances
        List<Balance> balances = trades.stream()
                .map(trade -> {
                    try {
                        return tradeService.getBalance(trade.getTradeId()).orElseThrow();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        // finds basket asset is in balances
        for (BasketAsset basketAsset : basket.getBasketAssets()) {
            if (basketAsset.getHoldingWeight().compareTo(BigDecimal.ZERO) == 0) {
                boolean ownedAsset = isOwnedAsset(basketAsset.getAssetId(), balances);
                if (!ownedAsset) {
                    BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                            .basketId(basket.getBasketId())
                            .assetId(basketAsset.getAssetId())
                            .build();
                    basketAssetRepository.deleteById(pk);
                    basketAssetRepository.flush();
                }
            }
        }
    }

    /**
     * checks owned asset
     * @param assetId asset id
     * @param balances balances
     * @return whether owned or not
     */
    boolean isOwnedAsset(String assetId, List<Balance> balances) {
        for (Balance balance : balances) {
            BalanceAsset balanceAsset = balance.getBalanceAsset(assetId).orElse(null);
            if (balanceAsset != null) {
                return true;
            }
        }
        return false;
    }

}
