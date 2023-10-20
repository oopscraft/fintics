package org.oopscraft.fintics.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator.Macd;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class AssetIndicatorTest {

    @Test
    void test() throws Throwable {
        // given
        String filePath = "org/oopscraft/fintics/model/AssetIndicatorTest.tsv";
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("time","open","high","low","close","5","10","20","60","120","MACD","Signal","MACD-Oscillator", "RSI", "RSI-Signal")
                .setSkipHeaderRecord(true)
                .build();
        final List<Double> inputCloses = new ArrayList<>();
        final List<Ohlcv> inputMinuteOhlcvs = new ArrayList<>();
        final List<Macd> inputMacds = new ArrayList<>();
        final List<Double> inputRsis = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        inputCloses.add(Double.parseDouble(record.get("close").replaceAll(",","")));
                        inputMinuteOhlcvs.add(Ohlcv.builder()
                                .openPrice(Double.parseDouble(record.get("open").replaceAll(",","")))
                                .highPrice(Double.parseDouble(record.get("high").replaceAll(",","")))
                                .lowPrice(Double.parseDouble(record.get("low").replaceAll(",","")))
                                .closePrice(Double.parseDouble(record.get("close").replaceAll(",","")))
                                .build());
                        inputMacds.add(Macd.builder()
                                .value(Double.parseDouble(record.get("MACD").replaceAll(",","")))
                                .signal(Double.parseDouble(record.get("Signal").replaceAll(",","")))
                                .oscillator(Double.parseDouble(record.get("MACD-Oscillator").replaceAll(",","")))
                                .build());
                        inputRsis.add(Double.parseDouble(record.get("RSI").replaceAll("[,%]","")));

                    });
        }

        // when
        List<Macd> outputMacds = new ArrayList<>();
        List<Double> outputRsis = new ArrayList<>();
        for(int i = 0, size = inputCloses.size(); i < size; i ++ ) {
            List<Ohlcv> ohlcvs = inputMinuteOhlcvs.subList(i, inputMinuteOhlcvs.size()-1);
            AssetIndicator assetIndicator = AssetIndicator.builder()
                    .asset(TradeAsset.builder()
                            .symbol("test")
                            .name("Test")
                            .build())
                    .minuteOhlcvs(ohlcvs)
                    .build();
            outputMacds.add(assetIndicator.getMinuteMacd(12,26,9));
            outputRsis.add(assetIndicator.getMinuteRsi(14));
        }

        // then
        for(int i = 0, size = inputCloses.size(); i < size; i ++ ) {
            log.debug("[{}] {}/{}, {}/{}",
                    i,
                    inputMacds.get(i).getOscillator(), inputRsis.get(i),
                    outputMacds.get(i).getOscillator(), inputRsis.get(i)
            );
        }
        for(int i = 0, size = inputCloses.size() - 70; i < size; i ++ ) {
            assertEquals(inputMacds.get(i).getOscillator(), outputMacds.get(i).getOscillator(), 0.02);
            assertEquals(inputRsis.get(i), outputRsis.get(i), 0.02);
        }
    }

}
