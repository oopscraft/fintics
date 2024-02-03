package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("monitor")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MONITOR')")
public class MonitorController {

    private final IndiceService indiceService;

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView getMonitor() {
        ModelAndView modelAndView = new ModelAndView("monitor.html");
        List<Indice> indices = indiceService.getIndices();
        modelAndView.addObject("indices", indices);
        List<Trade> trades = tradeService.getTrades().stream()
                        .filter(Trade::isEnabled)
                        .collect(Collectors.toList());
        modelAndView.addObject("trades", trades);
        return modelAndView;
    }

}
