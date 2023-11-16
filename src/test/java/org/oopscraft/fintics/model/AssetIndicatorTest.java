package org.oopscraft.fintics.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator.Dmi;
import org.oopscraft.fintics.calculator.Macd;

import java.io.InputStream;
import java.math.BigDecimal;
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
                .setHeader("time","open","high","low","close","MACD","MACD-Signal","MACD-Oscillator", "RSI", "RSI-Signal", "ADX", "PDI", "MDI")
                .setSkipHeaderRecord(true)
                .build();
        final List<BigDecimal> inputCloses = new ArrayList<>();
        final List<Ohlcv> inputMinuteOhlcvs = new ArrayList<>();
        final List<Macd> inputMacds = new ArrayList<>();
        final List<BigDecimal> inputRsis = new ArrayList<>();
        final List<Dmi> inputDmis = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        inputCloses.add(new BigDecimal(record.get("close").replaceAll(",","")));
                        inputMinuteOhlcvs.add(Ohlcv.builder()
                                .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
                                .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
                                .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
                                .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
                                .build());
                        inputMacds.add(Macd.builder()
                                .value(new BigDecimal(record.get("MACD").replaceAll(",","")))
                                .signal(new BigDecimal(record.get("MACD-Signal").replaceAll(",","")))
                                .oscillator(new BigDecimal(record.get("MACD-Oscillator").replaceAll(",","")))
                                .build());
                        inputRsis.add(new BigDecimal(record.get("RSI").replaceAll("[,%]","")));
                        inputDmis.add(Dmi.builder()
                                .pdi(new BigDecimal(record.get("PDI").replaceAll("[,%]","")))
                                .mdi(new BigDecimal(record.get("MDI").replaceAll("[,%]","")))
                                .adx(new BigDecimal(record.get("ADX").replaceAll("[,%]", "")))
                                .build());
                    });
        }

        // when
        List<Macd> outputMacds = new ArrayList<>();
        List<BigDecimal> outputRsis = new ArrayList<>();
        List<Dmi> outputDmis = new ArrayList<>();
        for(int i = 0, size = inputCloses.size(); i < size; i ++ ) {
            TradeAsset tradeAsset = TradeAsset.builder()
                    .symbol("test")
                    .name("Test")
                    .build();
            List<Ohlcv> ohlcvs = inputMinuteOhlcvs.subList(i, inputMinuteOhlcvs.size()-1);
            AssetIndicator assetIndicator = AssetIndicator.builder()
                    .symbol(tradeAsset.getSymbol())
                    .name(tradeAsset.getName())
                    .minuteOhlcvs(ohlcvs)
                    .build();
            outputMacds.add(assetIndicator.getMinuteMacd(12,26,9));
            outputRsis.add(assetIndicator.getMinuteRsi(14));
            outputDmis.add(assetIndicator.getMinuteDmi(14));
        }

        // then
        for(int i = 0, size = inputCloses.size(); i < size; i ++ ) {
            log.debug("[{}] {}/{}, {}/{}",
                    i,
                    inputMacds.get(i).getOscillator(), inputRsis.get(i),
                    outputMacds.get(i).getOscillator(), outputRsis.get(i)
            );
        }
        for(int i = 0, size = inputCloses.size() - 70; i < size; i ++ ) {
            assertEquals(inputMacds.get(i).getOscillator().doubleValue(), outputMacds.get(i).getOscillator().doubleValue(), 0.2);
            assertEquals(inputRsis.get(i).doubleValue(), outputRsis.get(i).doubleValue(), 2);
        }
    }

}
