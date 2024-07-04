package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Financial;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class FinancialResponse {

    private String assetId;

    private Instant datetime;

    private BigDecimal issuedShares;

    private BigDecimal totalAssets;

    private BigDecimal totalEquity;

    private BigDecimal netIncome;

    private BigDecimal eps;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal ebitda;

    private BigDecimal dividendYield;

    /**
     * factory method financial to financial response
     * @param financial financial model
     * @return financial response
     */
    public static FinancialResponse from(Financial financial) {
        return FinancialResponse.builder()
                .assetId(financial.getAssetId())
                .datetime(financial.getDatetime())
                .issuedShares(financial.getIssuedShares())
                .totalAssets(financial.getTotalAssets())
                .totalEquity(financial.getTotalEquity())
                .netIncome(financial.getNetIncome())
                .eps(financial.getEps())
                .per(financial.getPer())
                .roe(financial.getRoe())
                .roa(financial.getRoa())
                .ebitda(financial.getEbitda())
                .dividendYield(financial.getDividendYield())
                .build();
    }

}
