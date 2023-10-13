package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.Alarm;
import org.oopscraft.arch4j.core.alarm.AlarmSearch;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.ClientDefinitionRegistry;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
public class TradeController {

    private final AlarmService alarmService;

    @GetMapping
    public ModelAndView trade() {
        ModelAndView modelAndView = new ModelAndView("trade.html");
        modelAndView.addObject("clientDefinitions", ClientDefinitionRegistry.getClientDefinitions());

        List<Alarm> alarms = alarmService.getAlarms(AlarmSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("alarms", alarms);
        return modelAndView;
    }

}
