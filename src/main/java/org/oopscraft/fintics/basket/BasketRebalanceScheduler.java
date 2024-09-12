package org.oopscraft.fintics.basket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.service.AlarmService;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.dao.BasketAssetEntity;
import org.oopscraft.fintics.dao.BasketAssetRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.BasketService;
import org.oopscraft.fintics.service.TradeService;
import org.oopscraft.fintics.basket.BasketChange;
import org.oopscraft.fintics.basket.BasketRebalance;
import org.oopscraft.fintics.basket.BasketRebalanceContext;
import org.oopscraft.fintics.basket.BasketRebalanceFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketRebalanceScheduler {

    private final Map<String, Basket> scheduledBaskets = new HashMap<>();

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private final TaskScheduler taskScheduler;

    private final BasketRebalanceFactory basketRebalanceFactory;

    private final BasketService basketService;

    private final BasketAssetRepository basketAssetRepository;

    private final TradeService tradeService;

    private final FinticsProperties finticsProperties;

    private final AlarmService alarmService;

    public Basket getScheduledBasket(String basketId) {
        return scheduledBaskets.get(basketId);
    }

    public void startScheduledTask(Basket basket) {
        // removes task if already exist
        if (scheduledTasks.containsKey(basket.getBasketId())) {
            stopScheduledTask(basket);
        }
        // adds new schedule
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeTask(basket.getBasketId()),
                new CronTrigger(basket.getRebalanceSchedule())
        );
        scheduledBaskets.put(basket.getBasketId(), basket);
        scheduledTasks.put(basket.getBasketId(), scheduledFuture);
    }

    public void stopScheduledTask(Basket basket) {
        scheduledBaskets.remove(basket.getBasketId());
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(basket.getBasketId());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(basket.getBasketId());
        }
    }

    private synchronized void executeTask(String basketId) {
        try {
            Basket basket = basketService.getBasket(basketId).orElseThrow();
            BasketRebalanceContext context = BasketRebalanceContext.builder()
                    .basket(basket)
                    .build();
            BasketRebalance basketRebalance = basketRebalanceFactory.getObject(context);
            List<BasketChange> basketChanges = basketRebalance.getChanges();
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
            for (BasketChange basketChange : basketChanges) {
                String market = basket.getMarket();
                String symbol = basketChange.getSymbol();
                String assetId = String.format("%s.%s", market, symbol);
                BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                        .basketId(basketId)
                        .assetId(assetId)
                        .build();
                BasketAssetEntity basketAssetEntity = basketAssetRepository.findById(pk).orElse(null);
                if (basketAssetEntity == null) {
                    basketAssetEntity = BasketAssetEntity.builder()
                            .basketId(basketId)
                            .assetId(assetId)
                            .sort(sort++)
                            .enabled(true)
                            .build();
                }
                basketAssetEntity.setHoldingWeight(basketChange.getHoldingWeight());
                basketAssetRepository.saveAndFlush(basketAssetEntity);
            }

            // clear holdWeight zero + not holding
            basket = basketService.getBasket(basketId).orElseThrow();
            List<Trade> trades = tradeService.getTrades().stream()
                    .filter(trade -> Objects.equals(trade.getBasketId(), basketId))
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
                                .basketId(basketId)
                                .assetId(basketAsset.getAssetId())
                                .build();
                        basketAssetRepository.deleteById(pk);
                        basketAssetRepository.flush();
                    }
                }
            }
            sendSystemAlarm(String.format("Basket rebalance completed - %s", basket.getName()));
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
            sendSystemAlarm(e.getMessage());
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

    /**
     * send system alarm
     * @param content message content
     */
    protected void sendSystemAlarm(String content) {
        alarmService.sendAlarm(finticsProperties.getSystemAlarmId(), this.getClass().getSimpleName(), content);
    }

}
