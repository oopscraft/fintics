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
            BigDecimal mbb = smas.get(smas.size()-1);
            BigDecimal ubb = mbb.add(std.multiply(stdMultiplier));
            BigDecimal lbb = mbb.subtract(std.multiply(stdMultiplier));

            BigDecimal width = BigDecimal.ZERO;
            if (mbb.compareTo(BigDecimal.ZERO) != 0) {
                width = ubb.subtract(lbb)
                        .divide(mbb, getContext().getMathContext());
            }

            BigDecimal percentB = BigDecimal.ZERO;
            BigDecimal diffUbbLbb = ubb.subtract(lbb);
            if (diffUbbLbb.compareTo(BigDecimal.ZERO) != 0) {
                percentB = (closePrices.get(i).subtract(lbb))
                        .divide(diffUbbLbb, getContext().getMathContext())
                        .multiply(BigDecimal.valueOf(100));
            }

            BollingerBand bb = BollingerBand.builder()
                    .dateTime(series.get(i).getDateTime())
                    .middle(mbb.setScale(2, RoundingMode.HALF_UP))
                    .upper(ubb.setScale(2, RoundingMode.HALF_UP))
                    .lower(lbb.setScale(2, RoundingMode.HALF_UP))
                    .width(width.setScale(2, RoundingMode.HALF_UP))
                    .percentB(percentB.setScale(2, RoundingMode.HALF_UP))
                    .build();
            bbs.add(bb);
        }

        return bbs;
    }

}
