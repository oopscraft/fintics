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

@Controller
@RequestMapping("monitor")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MONITOR')")
public class MonitorController {

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView monitor() {
        ModelAndView modelAndView = new ModelAndView("monitor.html");
        List<Trade> trades = tradeService.getTrades();
        modelAndView.addObject("trades", trades);
        return modelAndView;
    }

}
