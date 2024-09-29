package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.DividendHistory;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendHistoryResponse {

    private LocalDate date;

    private String symbol;

    private String name;

    private BigDecimal dividendAmount;

    public static DividendHistoryResponse from(DividendHistory dividendHistory) {
        return DividendHistoryResponse.builder()
                .date(dividendHistory.getDate())
                .symbol(dividendHistory.getSymbol())
                .name(dividendHistory.getName())
                .dividendAmount(dividendHistory.getDividendAmount())
                .build();

    }

}
