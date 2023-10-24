package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Trade;
import org.springframework.stereotype.Component;

@Component("tradePermissionEvaluator")
@RequiredArgsConstructor
public class TradePermissionEvaluator {

    private final TradeService tradeService;

    public boolean hasAccessPermission(String tradeId) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        return trade.hasAccessPermission();
    }

    public boolean hasEditPermission(String tradeId) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        return trade.hasEditPermission();
    }

}
