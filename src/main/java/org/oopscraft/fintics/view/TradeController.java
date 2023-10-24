package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.Alarm;
import org.oopscraft.arch4j.core.alarm.AlarmSearch;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.ClientDefinitionRegistry;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.TradePermissionEvaluator;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
public class TradeController {

    private final TradeService tradeService;

    private final AlarmService alarmService;

    private final TradePermissionEvaluator tradePermissionEvaluator;

    @GetMapping
    public ModelAndView trade() {
        return new ModelAndView("trade.html");
    }

    @GetMapping("trade-detail")
    public ModelAndView tradeDetail(@RequestParam(value="tradeId", required = false) String tradeId) {

        // checks edit permission
        if(tradeId != null) {
            if(!tradePermissionEvaluator.hasAccessPermission(tradeId)) {
                throw new AccessDeniedException("Not Trade Owner");
            }
        }

        ModelAndView modelAndView = new ModelAndView("trade-detail.html");
        modelAndView.addObject("tradeId", tradeId);
        modelAndView.addObject("clientDefinitions", ClientDefinitionRegistry.getClientDefinitions());
        List<Alarm> alarms = alarmService.getAlarms(AlarmSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("alarms", alarms);
        return modelAndView;
    }

}
