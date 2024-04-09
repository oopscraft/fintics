package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.TradeRepository;
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
@RequiredArgsConstructor
public class SimulatesController {

    private final TradeRepository tradeRepository;

    @GetMapping("simulates")
    @PreAuthorize("hasAuthority('SIMULATES')")
    public ModelAndView getSimulates() {
        return new ModelAndView("simulates.html");
    }

}
