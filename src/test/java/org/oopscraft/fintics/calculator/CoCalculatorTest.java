package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CoCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/oopscraft/fintics/calculator/CoCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","volume","CO","Signal"}
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
        List<Co> cos = new CoCalculator(CoContext.DEFAULT)
                .calculate(ohlcvs);

        // then
        for(int i = 0, size = cos.size(); i < size; i ++) {
            Co co = cos.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpenPrice = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHighPrice = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLowPrice = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originVolume = new BigDecimal(inputRow.get("volume").replaceAll(",",""));
            BigDecimal originCo = new BigDecimal(inputRow.get("CO").replaceAll(",",""));
            BigDecimal originSignal = new BigDecimal(inputRow.get("Signal").replaceAll(",",""));

            log.info("[{}] {},{},{},{},{},{},{} / {},{},{},{},{},{},{}", i,
                    originOpenPrice, originHighPrice, originLowPrice, originClosePrice, originVolume, originCo, originSignal,
                    ohlcv.getOpenPrice(), ohlcv.getHighPrice(), ohlcv.getLowPrice(), ohlcv.getClosePrice(), ohlcv.getVolume(), co.getValue(), co.getSignal());

            // assert
            assertEquals(originCo.doubleValue(), co.getValue().doubleValue(), 0.1);
            assertEquals(originSignal.doubleValue(), co.getSignal().doubleValue(), 0.1);
        }

    }

}
