package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.service.BrokerService;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.StrategyService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trades")
@PreAuthorize("hasAuthority('TRADES')")
@RequiredArgsConstructor
public class TradesController {

    private final BrokerService brokerService;

    private final StrategyService strategyService;

    @GetMapping
    public ModelAndView getTrades() {
        ModelAndView modelAndView = new ModelAndView("trades.html");
        List<Broker> brokers =  brokerService.getBrokers(null, Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        List<Strategy> strategies = strategyService.getStrategies(null, Pageable.unpaged()).getContent();
        modelAndView.addObject("strategies", strategies);
        return modelAndView;
    }

}
