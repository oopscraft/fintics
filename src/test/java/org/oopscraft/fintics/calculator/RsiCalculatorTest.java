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
import java.math.RoundingMode;
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
        final List<BigDecimal> inputCloses = new ArrayList<>();
        final List<BigDecimal> inputRsis = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        inputCloses.add(new BigDecimal(record.get("close").replaceAll(",","")));
                        inputRsis.add(new BigDecimal(record.get("RSI").replaceAll("[,%]","")));
                    });
        }
        Collections.reverse(inputCloses);
        Collections.reverse(inputRsis);

        // when
        List<BigDecimal> outputRsis = RsiCalculator.of(inputCloses, 14).calculate().stream()
                .map(e -> e.setScale(2, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        for(int i = 0; i < inputCloses.size(); i++) {
            log.debug("[{}] {}, {}", i, inputRsis.get(i), outputRsis.get(i));
        }

        // then
        for(int i = 0; i < inputCloses.size(); i ++) {
            // period + 1 전의 RSI는 데이터부족으로 50으로 반환됨.
            if(i < 14 + 1) {
                assertEquals(50, outputRsis.get(i).doubleValue());
            }
            // 이후 부터는 값이 일치해야함.
            else{
                assertEquals(inputRsis.get(i).doubleValue(), outputRsis.get(i).doubleValue(), 0.02);
            }
        }
    }

}