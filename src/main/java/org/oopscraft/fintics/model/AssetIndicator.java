package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.calculator.MacdCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetIndicator extends Asset {

    private BigDecimal price;

    private BigDecimal bidPrice;

    private BigDecimal askPrice;

    @Builder.Default
    private List<AssetTransaction> dailyAssetTransactions = new ArrayList<>();

    @Builder.Default
    private List<AssetTransaction> hourlyAssetTransactions = new ArrayList<>();

    @Builder.Default
    private List<AssetTransaction> minuteAssetTransaction = new ArrayList<>();

    private BigDecimal getMacd(List<AssetTransaction> assetTransactions) {
        List<Double> prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());
        MacdCalculator macdCalculator = new MacdCalculator(prices);
        List<Double> macdHistogram = macdCalculator.calculateMacd(12, 26, 9);
        return BigDecimal.valueOf(macdHistogram.get(macdHistogram.size()-1));
    }

    public BigDecimal getDailyMacd() {
        return getMacd(dailyAssetTransactions);
    }

    public BigDecimal getHourlyMacd() {
        return getMacd(hourlyAssetTransactions);
    }

    public BigDecimal getMinuteMacd() {
        return getMacd(minuteAssetTransaction);
    }

}
