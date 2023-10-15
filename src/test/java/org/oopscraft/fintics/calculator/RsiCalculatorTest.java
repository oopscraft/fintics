package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RsiCalculatorTest {

    /**
     * 검증데이터는 한국투자증권 차트의 가격,RSI 데이터 (실제 한국증권 분봉 데이터를 사용함)
     */
    @Test
    void calculate() throws Throwable {
        // given
        String filePath = "org/oopscraft/fintics/calculator/RsiCalculatorTest.tsv";
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("time","open","high","low","close","5","10","20","60","120","RSI","Signal")
                .setSkipHeaderRecord(true)
                .build();
        final List<Double> closes = new ArrayList<>();
        final List<Double> rsis = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        closes.add(Double.parseDouble(record.get("close").replaceAll(",","")));
                        rsis.add(Double.parseDouble(record.get("RSI").replaceAll("[,%]","")));
                    });
        }
        Collections.reverse(closes);
        Collections.reverse(rsis);

        // when
        int period = 14; // RSI 주기
        RsiCalculator rsiCalculator = new RsiCalculator(closes, period);
        List<Double> rsiValues = rsiCalculator.calculate();
        for(int i = 0; i < closes.size(); i++) {
            log.debug("[{}] {}, {}", i, rsis.get(i), rsiValues.get(i));
        }

        // then
        for(int i = 0; i < closes.size(); i ++) {
            // period + 1 전의 RSI는 데이터부족으로 50으로 반환됨.
            if(i < period + 1) {
                assertEquals(50, rsiValues.get(i));
            }
            // 이후 부터는 값이 일치해야함.
            else{
                assertEquals(rsis.get(i), rsiValues.get(i), 0.02);
            }
        }
    }

}