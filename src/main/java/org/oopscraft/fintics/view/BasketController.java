package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Strategy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("basket")
@PreAuthorize("hasAuthority('BASKETS')")
@RequiredArgsConstructor
public class BasketController {

    @GetMapping
    public ModelAndView getBasket(@RequestParam(value = "basketId", required = false) String basketId) {
        ModelAndView modelAndView = new ModelAndView("basket.html");
        modelAndView.addObject("basketId", basketId);
        return modelAndView;
    }

}
