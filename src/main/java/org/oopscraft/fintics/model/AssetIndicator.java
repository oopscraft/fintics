package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.calculator.MacdCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    private Double price;

    private Boolean holdConditionResult;

    @Builder.Default
    private List<AssetTransaction> dailyAssetTransactions = new ArrayList<>();

    @Builder.Default
    private List<AssetTransaction> minuteAssetTransaction = new ArrayList<>();

    public Macd getDailyMacd() {
        return getMacd(dailyAssetTransactions);
    }

    public Macd getMinuteMacd() {
        return getMacd(minuteAssetTransaction);
    }

    public static Macd getMacd(List<AssetTransaction> assetTransactions) {
        List<Double> prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);
        List<Macd> macds = MacdCalculator.of(prices, 12, 26, 9)
                .calculate();
        return macds.get(macds.size()-1);
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