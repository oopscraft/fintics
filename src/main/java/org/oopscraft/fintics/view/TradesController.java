package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("trades")
@PreAuthorize("hasAuthority('TRADES')")
@RequiredArgsConstructor
public class TradesController {

    @GetMapping
    public ModelAndView getTrades() {
        return new ModelAndView("trades.html");
    }

}
