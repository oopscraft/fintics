package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MacdCalculatorTest {

    @Test
    void test() throws Throwable {
        // given
        String filePath = "org/oopscraft/fintics/calculator/MacdCalculatorTest.tsv";
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("time","open","high","low","close","5","10","20","60","120","MACD","Signal","MACD-Oscillator")
                .setSkipHeaderRecord(true)
                .build();
        final List<Double> inputCloses = new ArrayList<>();
        final List<Macd> inputMacds = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        inputCloses.add(Double.parseDouble(record.get("close").replaceAll(",","")));
                        inputMacds.add(Macd.builder()
                                .value(Double.parseDouble(record.get("MACD").replaceAll(",","")))
                                .signal(Double.parseDouble(record.get("Signal").replaceAll(",","")))
                                .oscillator(Double.parseDouble(record.get("MACD-Oscillator").replaceAll(",","")))
                                .build());
                    });
        }
        Collections.reverse(inputCloses);
        Collections.reverse(inputMacds);

        // when
        List<Macd> outputMacds = MacdCalculator.of(inputCloses, 12, 26, 9).calculate();
        for(int i = 0; i < inputCloses.size(); i++) {
            Macd inputMacd = inputMacds.get(i);
            Macd outputMacd = outputMacds.get(i);
            log.debug("[{}] {}/{}/{}, {}/{}/{}", i,
                    inputMacd.getValue(), inputMacd.getSignal(), inputMacd.getOscillator(),
                    outputMacd.getValue(), outputMacd.getSignal(), outputMacd.getOscillator());
        }

        // then
        for(int i = 0; i < inputCloses.size(); i ++) {
            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < (26*3) + 1) {
                continue;
            }
            // 이후 부터는 값이 일치해야함.
            Macd inputMacd = inputMacds.get(i);
            Macd outputMacd = outputMacds.get(i);
            assertEquals(inputMacd.getValue(), outputMacd.getValue(), 0.02);
            assertEquals(inputMacd.getSignal(), outputMacd.getSignal(), 0.02);
            assertEquals(inputMacd.getOscillator(), outputMacd.getOscillator(), 0.02);
        }
    }

}