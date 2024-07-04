package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.SimulateReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class SimulateReportResponse {

    @Builder.Default
    private List<TotalReturnResponse> totalReturns = new ArrayList<>();

    @Builder.Default
    private List<AssetReturnResponse> assetReturns = new ArrayList<>();

    private BigDecimal feeAmount;

    public static SimulateReportResponse from(SimulateReport simulateReport) {
        return SimulateReportResponse.builder()
                .totalReturns(simulateReport.getTotalReturns().stream()
                        .map(TotalReturnResponse::from)
                        .toList())
                .assetReturns(simulateReport.getAssetReturns().stream()
                        .map(AssetReturnResponse::from)
                        .toList())
                .feeAmount(simulateReport.getFeeAmount())
                .build();
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TotalReturnResponse {
        private LocalDateTime datetime;
        private BigDecimal totalAmount;
        public static TotalReturnResponse from(SimulateReport.TotalReturn totalReturn) {
            return TotalReturnResponse.builder()
                    .datetime(totalReturn.getDatetime())
                    .totalAmount(totalReturn.getTotalAmount())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class AssetReturnResponse {
        private String assetId;
        private String assetName;
        @Builder.Default
        private List<AssetProfitResponse> assetProfits = new ArrayList<>();
        public static AssetReturnResponse from(SimulateReport.AssetReturn assetReturn) {
            return AssetReturnResponse.builder()
                    .assetId(assetReturn.getAssetId())
                    .assetName(assetReturn.getAssetName())
                    .assetProfits(assetReturn.getAssetProfits().stream()
                            .map(AssetProfitResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Builder
    @Getter
    public static class AssetProfitResponse {
        private LocalDateTime datetime;
        private BigDecimal profitAmount;
        private BigDecimal accumulatedProfitAmount;
        public static AssetProfitResponse from(SimulateReport.AssetProfit assetProfit) {
            return AssetProfitResponse.builder()
                    .datetime(assetProfit.getDatetime())
                    .profitAmount(assetProfit.getProfitAmount())
                    .accumulatedProfitAmount(assetProfit.getAccumulatedProfitAmount())
                    .build();
        }
    }

}
