package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.FinancialRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinancialService {

    private final FinancialRepository financialRepository;

}
