package org.oopscraft.fintics.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

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
        final List<Double> closes = new ArrayList<>();
        final List<Double> macds = new ArrayList<>();
        final List<Double> signals = new ArrayList<>();
        final List<Double> macdOscillators = new ArrayList<>();
        final List<Double> rsis = new ArrayList<>();
        final List<Ohlcv> minuteOhlcvs = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        closes.add(Double.parseDouble(record.get("close").replaceAll(",","")));
                        macds.add(Double.parseDouble(record.get("MACD").replaceAll(",","")));
                        signals.add(Double.parseDouble(record.get("Signal").replaceAll(",","")));
                        macdOscillators.add(Double.parseDouble(record.get("MACD-Oscillator").replaceAll(",","")));
                        rsis.add(Double.parseDouble(record.get("RSI").replaceAll("[,%]","")));
                        minuteOhlcvs.add(Ohlcv.builder()
                                .openPrice(Double.parseDouble(record.get("open").replaceAll(",","")))
                                .highPrice(Double.parseDouble(record.get("high").replaceAll(",","")))
                                .lowPrice(Double.parseDouble(record.get("low").replaceAll(",","")))
                                .closePrice(Double.parseDouble(record.get("close").replaceAll(",","")))
                                .build());
                    });
        }

        // when
        AssetIndicator assetIndicator = AssetIndicator.builder()
                .asset(TradeAsset.builder()
                        .symbol("test")
                        .name("Test")
                        .build())
                .minuteOhlcvs(minuteOhlcvs)
                .build();

        // then
        for(int i = 0, size = assetIndicator.getMinuteOhlcvs().size(); i < size; i ++ ) {
            log.debug("[{}] {}/{}, {}/{}",
                    i,
                    macdOscillators.get(i), rsis.get(i),
                    assetIndicator.getMinuteMacdOscillator(i), assetIndicator.getMinuteRsi(i)
            );
        }
        for(int i = 0, size = assetIndicator.getMinuteOhlcvs().size() - 70; i < size; i ++ ) {
            assertEquals(macdOscillators.get(i), assetIndicator.getMinuteMacdOscillator(i), 0.02);
            assertEquals(rsis.get(i), assetIndicator.getMinuteRsi(i), 0.02);
        }
    }

}
