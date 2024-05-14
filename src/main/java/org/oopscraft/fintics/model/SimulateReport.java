package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateReport {

    @Getter
    private final List<TotalReturn> totalReturns = new ArrayList<>();

    @Getter
    private final List<AssetReturn> assetReturns = new ArrayList<>();

    @Getter
    private BigDecimal feeAmount = BigDecimal.ZERO;

    public synchronized void addTotalReturn(LocalDateTime dateTime, BigDecimal totalAmount) {
        LocalDate date = dateTime.toLocalDate();
        TotalReturn totalReturn = totalReturns.stream()
                .filter(it -> it.date.isEqual(date))
                .findFirst()
                .orElse(null);
        if (totalReturn == null) {
            this.totalReturns.add(TotalReturn.builder()
                    .date(date)
                    .totalAmount(totalAmount)
                    .build());
        } else {
            totalReturn.setTotalAmount(totalAmount);
        }
    }

    public synchronized void addAssetReturn(Asset asset, LocalDateTime dateTime, BigDecimal profitAmount) {
        AssetReturn assetReturn = this.assetReturns.stream()
                .filter(it -> it.getAssetId().equals(asset.getAssetId()))
                .findFirst()
                .orElse(null);
        if (assetReturn == null) {
            assetReturn = AssetReturn.builder()
                    .assetId(asset.getAssetId())
                    .assetName(asset.getAssetName())
                    .build();
            this.assetReturns.add(assetReturn);
        }
        List<AssetProfit> assetProfits = assetReturn.getAssetProfits();

        // calculate accumulated profit amount
        BigDecimal previousAccumulatedProfitAmount;
        if (assetProfits.isEmpty()) {
            previousAccumulatedProfitAmount = BigDecimal.ZERO;
        } else {
            previousAccumulatedProfitAmount = assetProfits
                    .get(assetProfits.size()-1)
                    .getAccumulatedProfitAmount();
        }
        BigDecimal accumulatedProfitAmount = previousAccumulatedProfitAmount.add(profitAmount);

        // add asset profit
        AssetProfit assetProfit = AssetProfit.builder()
                .dateTime(dateTime)
                .profitAmount(profitAmount)
                .accumulatedProfitAmount(accumulatedProfitAmount)
                .build();
        assetProfits.add(assetProfit);
    }

    public synchronized void addFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = this.feeAmount.add(feeAmount);
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TotalReturn {
        private LocalDate date;
        private BigDecimal totalAmount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AssetReturn {
        private String assetId;
        private String assetName;
        @Builder.Default
        private List<AssetProfit> assetProfits = new ArrayList<>();
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AssetProfit {
        LocalDateTime dateTime;
        BigDecimal profitAmount;
        BigDecimal accumulatedProfitAmount;
    }

}
