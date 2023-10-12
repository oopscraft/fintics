package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.ClientDefinitionRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
public class TradeController {

    @GetMapping
    public ModelAndView trade() {
        ModelAndView modelAndView = new ModelAndView("trade.html");
        modelAndView.addObject("clientDefinitions", ClientDefinitionRegistry.getClientDefinitions());
        return modelAndView;
    }

}
