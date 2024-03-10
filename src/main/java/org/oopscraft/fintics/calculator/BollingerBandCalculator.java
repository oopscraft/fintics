package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class BollingerBandCalculator extends Calculator<BollingerBandContext, BollingerBand> {

    public BollingerBandCalculator(BollingerBandContext context) {
        super(context);
    }

    @Override
    public List<BollingerBand> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
                .toList();

        List<BollingerBand> bbs = new ArrayList<>();
        BigDecimal stdMultiplier = BigDecimal.valueOf(getContext().getStdMultiplier());

        for (int i = 0; i < closePrices.size(); i ++) {
            List<BigDecimal> periodClosePrices = closePrices.subList(
                    Math.max(i - getContext().getPeriod(), 0),
                    i + 1
            );

            List<BigDecimal> stds = stds(periodClosePrices, periodClosePrices.size(), getContext().getMathContext());
            BigDecimal std = stds.get(stds.size()-1);

            List<BigDecimal> smas = smas(periodClosePrices, periodClosePrices.size(), getContext().getMathContext());
            BigDecimal middle = smas.get(smas.size()-1);
            BigDecimal upper = middle.add(std.multiply(stdMultiplier));
            BigDecimal lower = middle.subtract(std.multiply(stdMultiplier));

            BigDecimal width = BigDecimal.ZERO;
            if (middle.compareTo(BigDecimal.ZERO) != 0) {
                width = upper.subtract(lower)
                        .divide(middle, getContext().getMathContext());
            }

            BigDecimal percentB = BigDecimal.ZERO;
            BigDecimal diffUpperLower = upper.subtract(lower);
            if (diffUpperLower.compareTo(BigDecimal.ZERO) != 0) {
                percentB = (closePrices.get(i).subtract(lower))
                        .divide(diffUpperLower, getContext().getMathContext())
                        .multiply(BigDecimal.valueOf(100));
            }

            BollingerBand bb = BollingerBand.builder()
                    .dateTime(series.get(i).getDateTime())
                    .middle(middle)
                    .upper(upper)
                    .lower(lower)
                    .width(width.setScale(2, RoundingMode.HALF_UP))
                    .percentB(percentB.setScale(2, RoundingMode.HALF_UP))
                    .build();
            bbs.add(bb);
        }

        return bbs;
    }

}
