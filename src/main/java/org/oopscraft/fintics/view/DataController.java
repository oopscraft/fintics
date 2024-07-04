package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("data")
@PreAuthorize("hasAuthority('DATA')")
@RequiredArgsConstructor
public class DataController {

    private final TradeService tradeService;

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getData() {
        ModelAndView modelAndView = new ModelAndView("data.html");
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        // assets
        List<Asset> assets = new ArrayList<>();
        tradeService.getTrades().forEach(trade -> {
            assets.addAll(trade.getTradeAssets());
        });
        modelAndView.addObject("assets", assets);
        // return
        return modelAndView;
    }

}
