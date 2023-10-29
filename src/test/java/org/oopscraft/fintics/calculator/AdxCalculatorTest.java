package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
class AdxCalculatorTest extends AbstractCalculatorTest {

    void calculate(String fileName) {
        // given
        List<Map<String,String>> inputList = readTsv(
                "org/oopscraft/fintics/calculator/" + fileName,
                new String[]{"time","open","high","low","close","adx","pdi","mdi"});
        Collections.reverse(inputList);
        List<Double> highSeries = inputList.stream()
                .map(map -> Double.parseDouble(map.get("high").replaceAll(",","")))
                .collect(Collectors.toList());
        List<Double> lowSeries = inputList.stream()
                .map(map -> Double.parseDouble(map.get("low").replaceAll(",","")))
                .collect(Collectors.toList());
        List<Double> closeSeries = inputList.stream()
                .map(map -> Double.parseDouble(map.get("close").replaceAll(",","")))
                .collect(Collectors.toList());

        // when
        List<Adx> dmis = AdxCalculator.of(highSeries, lowSeries, closeSeries, 14)
                .calculate();

        // then
        for(int i = 0; i < dmis.size(); i ++) {
            Map<String,String> row  = inputList.get(i);
            Adx dmi = dmis.get(i);
            log.info("[{}][{}|{}|{}|{}] {}\t{}\t{} | {}\t{}\t{}",
                    i, row.get("time"), row.get("high"), row.get("low"), row.get("close"),
                    row.get("adx"), row.get("pdi"), row.get("mdi"),
                    dmi.getValue(), dmi.getPdi(), dmi.getMdi()
            );
        }
    }

    @Test
    @Order(2)
    void calculate_KODEX코스닥150() {
        calculate("AdxCalculatorTest.KODEX코스닥150.tsv");
    }

    @Test
    @Order(3)
    void calculate_KODEX코스닥150선물인버스() {
        calculate("AdxCalculatorTest.KODEX코스닥150선물인버스.tsv");
    }

}