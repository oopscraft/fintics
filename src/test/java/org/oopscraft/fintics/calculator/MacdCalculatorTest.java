package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        final List<Double> closes = new ArrayList<>();
        final List<Double> macds = new ArrayList<>();
        final List<Double> signals = new ArrayList<>();
        final List<Double> macdOscillators = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        closes.add(Double.parseDouble(record.get("close").replaceAll(",","")));
                        macds.add(Double.parseDouble(record.get("MACD").replaceAll(",","")));
                        signals.add(Double.parseDouble(record.get("Signal").replaceAll(",","")));
                        macdOscillators.add(Double.parseDouble(record.get("MACD-Oscillator").replaceAll(",","")));
                    });
        }
        Collections.reverse(closes);
        Collections.reverse(macds);
        Collections.reverse(signals);
        Collections.reverse(macdOscillators);

        // when
        int shortTermPeriod = 12;
        int longTermPeriod = 26;
        int signalPeriod = 9;
        MacdCalculator macdCalculator = new MacdCalculator(closes, shortTermPeriod, longTermPeriod, signalPeriod);
        List<Macd> macdValues = macdCalculator.calculate();
        for(int i = 0; i < closes.size(); i++) {
            log.debug("[{}] {}/{}/{}, {}/{}/{}", i,
                    macds.get(i), signals.get(i), macdOscillators.get(i),
                    macdValues.get(i).getMacd(), macdValues.get(i).getSignal(), macdValues.get(i).getOscillator());
        }

        // then
        for(int i = 0; i < closes.size(); i ++) {
            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < (longTermPeriod*3) + 1) {
                continue;
            }
            // 이후 부터는 값이 일치해야함.
            Macd macd = macdValues.get(i);
            assertEquals(macds.get(i), macd.getMacd(), 0.02);
            assertEquals(signals.get(i), macd.getSignal(), 0.02);
            assertEquals(macdOscillators.get(i), macd.getOscillator(), 0.02);
        }
    }

}