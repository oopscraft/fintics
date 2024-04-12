package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.Alarm;
import org.oopscraft.arch4j.core.alarm.AlarmSearch;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.service.BrokerService;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.StrategyService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@PreAuthorize("hasAuthority('TRADES')")
@RequiredArgsConstructor
public class TradeController {

    private final AlarmService alarmService;

    private final IndiceService indiceService;

    private final BrokerService brokerService;

    private final StrategyService strategyService;

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getTrade(@RequestParam(value="tradeId", required = false) String tradeId) {
        ModelAndView modelAndView = new ModelAndView("trade.html");
        modelAndView.addObject("tradeId", tradeId);
        List<Alarm> alarms = alarmService.getAlarms(AlarmSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("alarms", alarms);
        List<Broker> brokers =  brokerService.getBrokers(null, Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        List<BrokerClientDefinition> brokerClientDefinitions = brokerClientDefinitionRegistry.getBrokerClientDefinitions();
        modelAndView.addObject("brokerClientDefinitions", brokerClientDefinitions);
        List<Strategy> strategies = strategyService.getStrategies(null, Pageable.unpaged()).getContent();
        modelAndView.addObject("strategies", strategies);
        modelAndView.addObject("indices", indiceService.getIndices());
        modelAndView.addObject("simulateStatus", Simulate.Status.values());
        modelAndView.addObject("orderKinds", Order.Kind.values());
        return modelAndView;
    }

}
