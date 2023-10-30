package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class DmiCalculatorTest extends AbstractCalculatorTest {

    void calculate(String fileName) {
        // given
        List<Map<String,String>> inputList = readTsv(
                "org/oopscraft/fintics/calculator/" + fileName,
                new String[]{"time","open","high","low","close","adx","pdi","mdi"});
        Collections.reverse(inputList);
        List<BigDecimal> highSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("high").replaceAll(",","")))
                .collect(Collectors.toList());
        List<BigDecimal> lowSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("low").replaceAll(",","")))
                .collect(Collectors.toList());
        List<BigDecimal> closeSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("close").replaceAll(",","")))
                .collect(Collectors.toList());
        List<BigDecimal> adxSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("adx").replaceAll("[,%]","")))
                .collect(Collectors.toList());
        List<BigDecimal> pdiSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("pdi").replaceAll("[,%]","")))
                .collect(Collectors.toList());
        List<BigDecimal> mdiSeries = inputList.stream()
                .map(map -> new BigDecimal(map.get("mdi").replaceAll("[,%]","")))
                .collect(Collectors.toList());

        // when
        List<Dmi> dmis = DmiCalculator.of(highSeries, lowSeries, closeSeries, 14)
                .calculate();

        // then
        for(int i = 0; i < dmis.size(); i ++) {
            Map<String,String> row  = inputList.get(i);
            Dmi dmi = dmis.get(i);
            log.info("[{}][{}|{}|{}|{}] {}\t{}\t{} | {}\t{}\t{}",
                    i, row.get("time"), row.get("high"), row.get("low"), row.get("close"),
                    row.get("adx"), row.get("pdi"), row.get("mdi"),
                    dmi.getAdx(), dmi.getPdi(), dmi.getMdi()
            );
        }
        for(int i = 0; i < highSeries.size(); i ++) {
            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < 50) {
                continue;
            }
            // 이후 부터는 값이 일치해야함.
            Dmi dmi = dmis.get(i);
            assertEquals(dmi.getAdx().doubleValue(), adxSeries.get(i).doubleValue(), 1);
            assertEquals(dmi.getPdi().doubleValue(), pdiSeries.get(i).doubleValue(), 1);
            assertEquals(dmi.getMdi().doubleValue(), mdiSeries.get(i).doubleValue(), 1);
        }
    }

    @Test
    @Order(2)
    void calculate_KODEX코스닥150() {
        calculate("DmiCalculatorTest.KODEX코스닥150.tsv");
    }

    @Test
    @Order(3)
    void calculate_KODEX코스닥150선물인버스() {
        calculate("DmiCalculatorTest.KODEX코스닥150선물인버스.tsv");
    }

}