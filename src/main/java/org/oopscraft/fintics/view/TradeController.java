package org.oopscraft.fintics.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("trade")
public class TradeController {

    @GetMapping
    public ModelAndView fintics() {
        return new ModelAndView("trade.html");
    }

}
