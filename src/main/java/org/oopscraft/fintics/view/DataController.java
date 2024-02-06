package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.trade.TradeClientDefinition;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("data")
@PreAuthorize("hasAuthority('DATA')")
@RequiredArgsConstructor
public class DataController {

    @GetMapping
    public ModelAndView getData() {
        ModelAndView modelAndView = new ModelAndView("data.html");
        // exchange ids
        List<String> exchangeIds = TradeClientFactory.getTradeClientDefinitions().stream()
                .map(TradeClientDefinition::getExchangeId)
                .distinct()
                .toList();
        modelAndView.addObject("exchangeIds", exchangeIds);
        return modelAndView;
    }

}
