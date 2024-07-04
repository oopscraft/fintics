package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("monitors")
@PreAuthorize("hasAuthority('MONITORS')")
@RequiredArgsConstructor
public class MonitorsController {

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView getMonitor() {
        ModelAndView modelAndView = new ModelAndView("monitors.html");
        List<Trade> trades = tradeService.getTrades().stream()
                        .filter(Trade::isEnabled)
                        .collect(Collectors.toList());
        modelAndView.addObject("trades", trades);
        return modelAndView;
    }

}
