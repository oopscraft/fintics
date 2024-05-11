package org.oopscraft.fintics.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimulateReport {

    @Getter
    private Map<LocalDate, BigDecimal> totalAmounts = new LinkedHashMap<>();

    public void snapshotBalance(LocalDateTime dateTime, Balance balance) {
        LocalDate date = dateTime.toLocalDate();
        totalAmounts.put(date, balance.getTotalAmount());
    }

}
