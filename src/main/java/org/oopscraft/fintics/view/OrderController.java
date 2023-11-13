package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ORDER')")
public class OrderController {

    @GetMapping
    public ModelAndView orderHistory() {
        return new ModelAndView("order.html");
    }

}
