package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.SimulateReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Builder
@Getter
public class SimulateReportResponse {

    @Builder.Default
    private Map<LocalDate, BigDecimal> totalAmounts = new LinkedHashMap<>();

    public static SimulateReportResponse from(SimulateReport simulateReport) {
        return SimulateReportResponse.builder()
                .totalAmounts(simulateReport.getTotalAmounts())
                .build();
    }

}
