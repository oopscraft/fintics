package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.MacdCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AssetIndicator extends Asset {

    private LocalDateTime collectedAt;

    private BigDecimal price;

    @Builder.Default
    private List<AssetTransaction> dailyAssetTransactions = new ArrayList<>();

    @Builder.Default
    private List<AssetTransaction> minuteAssetTransaction = new ArrayList<>();

    public BigDecimal getDailyMacd() {
        return getMacd(dailyAssetTransactions);
    }

    public BigDecimal getMinuteMacd() {
        return getMacd(minuteAssetTransaction);
    }

    public static BigDecimal getMacd(List<AssetTransaction> assetTransactions) {
        List<BigDecimal> prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());
        List<BigDecimal> macdValues = MacdCalculator.calculate(prices, 12, 26, 9);
        return macdValues.get(macdValues.size()-1);
    }

    public BigDecimal getDailyRsi() {
        return getRsi(dailyAssetTransactions);
    }

    public BigDecimal getMinuteRsi() {
        return getRsi(minuteAssetTransaction);
    }

    public static BigDecimal getRsi(List<AssetTransaction> assetTransactions) {
        return BigDecimal.ZERO;
    }

}