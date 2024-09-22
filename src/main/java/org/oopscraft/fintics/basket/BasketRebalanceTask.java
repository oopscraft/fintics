package org.oopscraft.fintics.basket;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.BasketService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Builder
@Slf4j
public class BasketRebalanceTask {

    private final Basket basket;

    private final BasketService basketService;

    private final TradeService tradeService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    public void execute() {
        BasketScriptRunner basketRebalance = basketScriptRunnerFactory.getObject(basket);
        List<BasketRebalanceResult> basketChanges = basketRebalance.run();
        log.info("basketChanges: {}", basketChanges);

        //================================================
        // 0. 베스켓 사용 중인 트레이드 + 잔고 조회
        //================================================
        List<Trade> trades = tradeService.getTrades(TradeSearch.builder().build(), Pageable.unpaged()).stream()
                .filter(trade -> Objects.equals(trade.getBasketId(), basket.getBasketId()))
                .toList();
        List<Balance> balances = trades.stream()
                .map(trade -> {
                    try {
                        return tradeService.getBalance(trade.getTradeId()).orElseThrow();
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        //===========================================
        // 신규 리밸런싱 종목 추가
        //===========================================
        for (BasketRebalanceResult basketChange : basketChanges) {
            String market = basket.getMarket();
            String symbol = basketChange.getSymbol();
            BigDecimal holdingWeight = basketChange.getHoldingWeight();
            String assetId = String.format("%s.%s", market, symbol);
            // 동일 종목 베스켓 에서 조회
            BasketAsset basketAsset = basket.getBasketAssets().stream()
                    .filter(it -> Objects.equals(it.getAssetId(), assetId))
                    .findFirst()
                    .orElse(null);
            // 신규 종목인 경우 추가
            if (basketAsset == null) {
                basketAsset = BasketAsset.builder()
                        .assetId(assetId)
                        .holdingWeight(holdingWeight)
                        .build();
                basket.getBasketAssets().add(basketAsset);
            } else {
                // 이미 존재 하는 종목인 경우 고정 종목이 아니 라면 보유 비중 수정
                if (!basketAsset.isFixed()) {
                    basketAsset.setHoldingWeight(holdingWeight);
                }
            }
        }

        //===========================================
        // 기존 종목 삭제
        //===========================================
        for (int i = basket.getBasketAssets().size()-1; i >= 0; i --) {
            BasketAsset basketAsset = basket.getBasketAssets().get(i);
            // 고정 종목은 리벨런싱 제외
            if (basketAsset.isFixed()) {
                continue;
            }
            // 교체 종목 인지 여부 확인
            boolean existInBasketChanges = basketChanges.stream().anyMatch(it ->
                    Objects.equals(it.getSymbol(), basketAsset.getSymbol()));
            // 교체 종목이 아닌 경우 (삭제 대상)
            if (!existInBasketChanges) {
                // 현재 매수 상태 인지 여부 확인
                boolean ownedAsset = isOwnedAsset(basketAsset.getAssetId(), balances);
                // 매수 상태인 경우 보유 비중만 0으로 설정 (매도 후 추가 매수는 되지 않고 다음 차 리밸런싱 시 삭제됨)
                if (ownedAsset) {
                    basketAsset.setHoldingWeight(BigDecimal.ZERO);
                }
                // 매수 하지 않은 종목인 경우 바로 삭제
                else {
                    basket.getBasketAssets().remove(i);
                }
            }
        }

        //=============================================
        // 최종 변경 사항 저장 처리
        //=============================================
        basketService.saveBasket(basket);
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
