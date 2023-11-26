package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator._legacy.ObvCalculator;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CmfCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/oopscraft/fintics/calculator/CmfCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","volume","CMF"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .closePrice(new BigDecimal(row.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(row.get("volume").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<BigDecimal> cmfs = ObvCalculator.of(ohlcvs).calculate();

        // then
        for(int i = 0, size = cmfs.size(); i < size; i ++) {
            BigDecimal cmf = cmfs.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originVolume = new BigDecimal(inputRow.get("volume").replaceAll(",",""));
            BigDecimal originAd = new BigDecimal(inputRow.get("CMF").replaceAll(",",""));

            log.info("[{}] {},{},{} / {},{},{}", i,
                    originClosePrice, originVolume, originAd,
                    ohlcv.getClosePrice(), ohlcv.getVolume(), cmf);
            //assertEquals(originAd, cmf);
        }

    }

}
