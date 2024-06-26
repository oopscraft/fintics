package org.oopscraft.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class WiiliamsRCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() throws Throwable {
        // given
        String filePath = "org/oopscraft/fintics/indicator/WilliamsRCalculatorTest.tsv";
        String[] columnNames = new String[]{"time","open","high","low","close","volume","r","signal"};
        List<Map<String,String>> rows = readTsv(filePath, columnNames);
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm","open","high","low","close",null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<WilliamsR> williamsRs = new WilliamsRCalculator(WilliamsRContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < rows.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            BigDecimal originCci = new BigDecimal(row.get("r").replaceAll("[,%]",""));
            BigDecimal originSignal = new BigDecimal(row.get("signal").replaceAll("[,%]", ""));
            WilliamsR williamsR = williamsRs.get(i);
            log.debug("[{}]{}|{}/{}|{}/{}", i, row.get("time"), originCci, originSignal, williamsR.getValue(), williamsR.getSignal());

            // assert
            if (i >= 20) {
                assertEquals(originCci.doubleValue(), williamsR.getValue().doubleValue(), 0.9);
                assertEquals(originCci.doubleValue(), williamsR.getValue().doubleValue(), 0.9);
            }

        }
    }

}