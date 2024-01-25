package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("simulate")
@RequiredArgsConstructor
public class SimulateController {

    @GetMapping
    public ModelAndView getSimulate() {
        return new ModelAndView("simulate.html");
    }

    @GetMapping("simulate-detail")
    public ModelAndView getSimulateDetail() {
        return new ModelAndView("simulate-detail.html");
    }

}
