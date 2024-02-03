package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("data")
@PreAuthorize("hasAuthority('DATA')")
@RequiredArgsConstructor
public class DataController {

    @GetMapping
    public ModelAndView getData() {
        return new ModelAndView("data.html");
    }

}
