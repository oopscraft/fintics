package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("orders")
@PreAuthorize("hasAuthority('ORDERS')")
@RequiredArgsConstructor
public class OrdersController {

    @GetMapping
    public ModelAndView getOrders() {
        return new ModelAndView("orders.html");
    }

}
