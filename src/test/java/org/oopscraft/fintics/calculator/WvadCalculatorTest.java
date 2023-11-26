package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class WvadCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/oopscraft/fintics/calculator/WvadCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","volume","WVAD"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .openPrice(new BigDecimal(row.get("open").replaceAll(",","")))
                            .highPrice(new BigDecimal(row.get("high").replaceAll(",", "")))
                            .lowPrice(new BigDecimal(row.get("low").replaceAll(",","")))
                            .closePrice(new BigDecimal(row.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(row.get("volume").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<Wvad> wvads = new WvadCalculator(WvadContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0, size = wvads.size(); i < size; i ++) {
            Wvad wvad = wvads.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpenPrice = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHighPrice = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLowPrice = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originVolume = new BigDecimal(inputRow.get("volume").replaceAll(",",""));
            BigDecimal originWvad = new BigDecimal(inputRow.get("WVAD").replaceAll(",",""));

            log.info("[{}] {},{},{},{},{},{} / {},{},{},{},{},{}", i,
                    originOpenPrice, originHighPrice, originLowPrice, originClosePrice, originVolume, originWvad,
                    ohlcv.getOpenPrice(), ohlcv.getHighPrice(), ohlcv.getLowPrice(), ohlcv.getClosePrice(), ohlcv.getVolume(), wvad);
            assertEquals(originWvad.doubleValue(), wvad.getValue().doubleValue(), 1.0);
        }

    }

}
