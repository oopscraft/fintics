package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Financial;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class AssetFinancialResponse {

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

    public static AssetFinancialResponse from(Financial assetFinancial) {
        return AssetFinancialResponse.builder()
                .assetId(assetFinancial.getAssetId())
                .datetime(assetFinancial.getDatetime())
                .issuedShares(assetFinancial.getIssuedShares())
                .totalAssets(assetFinancial.getTotalAssets())
                .totalEquity(assetFinancial.getTotalEquity())
                .netIncome(assetFinancial.getNetIncome())
                .eps(assetFinancial.getEps())
                .per(assetFinancial.getPer())
                .roe(assetFinancial.getRoe())
                .roa(assetFinancial.getRoa())
                .ebitda(assetFinancial.getEbitda())
                .dividendYield(assetFinancial.getDividendYield())
                .build();
    }

}
