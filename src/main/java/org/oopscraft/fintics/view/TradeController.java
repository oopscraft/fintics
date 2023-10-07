package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView fintics() {
        return new ModelAndView("trade.html");
    }

    @GetMapping("get-trades")
    @ResponseBody
    public List<Trade> getTrades() {
        return tradeService.getTrades();
    }

    @GetMapping("get-trade")
    @ResponseBody
    public Trade getTrade(@RequestParam("tradeId") String tradeId) {
        return tradeService.getTrade(tradeId).orElseThrow();
    }

}
