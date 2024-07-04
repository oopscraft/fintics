package org.oopscraft.fintics.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financials")
@PreAuthorize("hasAuthority('API_FINANCIAL')")
@Tag(name = "financials", description = "financials")
public class FinancialRestController {
}
