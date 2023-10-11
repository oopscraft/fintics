package org.oopscraft.fintics.model;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        // real
        double[] prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
        ArrayUtils.reverse(prices);

        Core core = new Core();
        int startIdx = 0;
        int endIdx = prices.length - 1;
        double[] inReal = prices;
        int optInFastPeriod = 6;
        int optInSlowPeriod = 13;
        int optInSignalPeriod = 4;
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        double[] outMACD = new double[prices.length];
        double[] outMACDSignal = new double[prices.length];
        double[] outMACDHist = new double[prices.length];

        RetCode retCode = core.macdExt(
                startIdx,
                endIdx,
                inReal,
                optInFastPeriod,
                MAType.Ema,
                optInSlowPeriod,
                MAType.Ema,
                optInSignalPeriod,
                MAType.Ema,
                outBegIdx,
                outNBElement,
                outMACD,
                outMACDSignal,
                outMACDHist
        );

        if (!retCode.name().equals("Success")) {
            throw new RuntimeException(retCode.name());
        }

        double currentMacd = outMACDHist[Math.max(0, outNBElement.value - 1)];
        log.debug("== currentMacd:{}", currentMacd);
        return BigDecimal.valueOf(currentMacd)
                .setScale(2, RoundingMode.FLOOR);
    }

    public BigDecimal getDailyRsi() {
        return getRsi(dailyAssetTransactions);
    }

    public BigDecimal getMinuteRsi() {
        return getRsi(minuteAssetTransaction);
    }

    public static BigDecimal getRsi(List<AssetTransaction> assetTransactions) {

        // real
        double[] prices = assetTransactions.stream()
                .map(AssetTransaction::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
        ArrayUtils.reverse(prices);

        Core core = new Core();
        int startIdx = 0;
        int endIdx = prices.length - 1;
        double[] inReal = prices;
        int optInTimePeriod = 14;
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        double[] outReal = new double[prices.length];

        RetCode retCode = core.rsi(
                startIdx,
                endIdx,
                inReal,
                optInTimePeriod,
                outBegIdx,
                outNBElement,
                outReal
        );

        if (!retCode.name().equals("Success")) {
            throw new RuntimeException(retCode.name());
        }

        double currentRsi = outReal[Math.max(0, outNBElement.value - 1)];
        log.debug("== currentRsi:{}", currentRsi);
        return BigDecimal.valueOf(currentRsi)
                .setScale(2, RoundingMode.FLOOR);
    }

}