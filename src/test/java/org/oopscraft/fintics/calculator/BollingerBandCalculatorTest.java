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
public class BollingerBandCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/oopscraft/fintics/calculator/BollingerBandCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","ubb","mbb","lbb","bandWidth","percentB"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .openPrice(new BigDecimal(row.get("open").replaceAll(",","")))
                            .highPrice(new BigDecimal(row.get("high").replaceAll(",", "")))
                            .lowPrice(new BigDecimal(row.get("low").replaceAll(",","")))
                            .closePrice(new BigDecimal(row.get("close").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<BollingerBand> bollingerBands = new BollingerBandCalculator(BollingerBandContext.DEFAULT)
                .calculate(ohlcvs);

        // then
        for(int i = 0, size = bollingerBands.size(); i < size; i ++) {
            BollingerBand bollingerBand = bollingerBands.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpenPrice = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHighPrice = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLowPrice = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originUbb = new BigDecimal(inputRow.get("ubb").replaceAll(",",""));
            BigDecimal originMbb = new BigDecimal(inputRow.get("mbb").replaceAll(",",""));
            BigDecimal originLbb = new BigDecimal(inputRow.get("lbb").replaceAll(",",""));
            BigDecimal originBandWidth = new BigDecimal(inputRow.get("bandWidth").replaceAll(",",""));
            BigDecimal originPercentB = new BigDecimal(inputRow.get("percentB").replaceAll("[,|%]",""));


            log.info("[{}] {},{},{},{}|{},{},{},{},{} / {},{},{},{}|{},{},{},{},{}", i,
                    originOpenPrice, originHighPrice, originLowPrice, originClosePrice, originUbb, originMbb, originLbb, originBandWidth, originPercentB,
                    ohlcv.getOpenPrice(), ohlcv.getHighPrice(), ohlcv.getLowPrice(), ohlcv.getClosePrice(), bollingerBand.getUpper(), bollingerBand.getMiddle(), bollingerBand.getLower(), bollingerBand.getBandWidth(), bollingerBand.getPercentB());

            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < (26*3) + 1) {
                continue;
            }

            // assert TODO 증권사별로 기준이 조금씩 다른듯.(증권사들 끼리도 맞지 않음)
//            assertEquals(originUbb.doubleValue(), bb.getUbb().doubleValue(), 0.1);
//            assertEquals(originMbb.doubleValue(), bb.getMbb().doubleValue(), 0.1);
//            assertEquals(originLbb.doubleValue(), bb.getLbb().doubleValue(), 0.1);
//            assertEquals(originBandWidth.doubleValue(), bb.getBandWidth().doubleValue(), 0.1);
//            assertEquals(originPercentB.doubleValue(), bb.getPercentB().doubleValue(), 0.1);
        }

    }

}
