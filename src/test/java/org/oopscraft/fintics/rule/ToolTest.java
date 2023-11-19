package org.oopscraft.fintics.rule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator.Dmi;
import org.oopscraft.fintics.calculator.Macd;
import org.oopscraft.fintics.model.Ohlcv;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ToolTest {

    @SuperBuilder
    @Setter
    @Getter
    public static class FileOhlcv extends Ohlcv {
        private Macd macd;
        private BigDecimal rsi;
        private Dmi dmi;
    }

    private List<FileOhlcv> loadTestOhlcvFile() {
        String filePath = "org/oopscraft/fintics/rule/ToolTest.tsv";
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("time","open","high","low","close","MACD","MACD-Signal","MACD-Oscillator", "RSI", "RSI-Signal", "ADX", "PDI", "MDI")
                .setSkipHeaderRecord(true)
                .build();
        final List<FileOhlcv> rows = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        FileOhlcv row = FileOhlcv.builder()
                                .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
                                .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
                                .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
                                .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
                                .build();
                        row.setMacd(Macd.builder()
                                .value(new BigDecimal(record.get("MACD").replaceAll(",","")))
                                .signal(new BigDecimal(record.get("MACD-Signal").replaceAll(",","")))
                                .oscillator(new BigDecimal(record.get("MACD-Oscillator").replaceAll(",","")))
                                .build());
                        row.setRsi(new BigDecimal(record.get("RSI").replaceAll("[,%]","")));
                        row.setDmi(Dmi.builder()
                                .pdi(new BigDecimal(record.get("PDI").replaceAll("[,%]","")))
                                .mdi(new BigDecimal(record.get("MDI").replaceAll("[,%]","")))
                                .adx(new BigDecimal(record.get("ADX").replaceAll("[,%]", "")))
                                .build());
                        rows.add(row);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    @Test
    void zScoreNormal() {
        // given
        List<BigDecimal> values = Stream.of(10040, 10020, 10030, 9990, 10020, 10030, 10070, 10100, 10090, 10080, 10100, 10000)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal zScore = tool.zScore(values, BigDecimal.valueOf(10040));

        // then
        log.info("zScore:{}", zScore.doubleValue());
        assertTrue(zScore.doubleValue() <= 1);
    }

    @Test
    void zScoreOutlier() {
        // given
        List<BigDecimal> values = Stream.of(10100, 10020, 10030, 9990, 10020, 10030, 10070, 10100, 10090, 10080, 10100, 10000)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal zScore = tool.zScore(values, BigDecimal.valueOf(10110));

        // then
        log.info("zScore:{}", zScore.doubleValue());
        assertTrue(zScore.doubleValue() > 1);
    }

    @Test
    void slope() {
        // given
        List<BigDecimal> values = Stream.of(20,10)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal slope = tool.slope(values);

        // then
        log.info("== slope:{}", slope);
        assertTrue(slope.doubleValue() > 1);
    }

    @Test
    @Order(1)
    void average() {
        // given
        List<BigDecimal> values = Stream.of(10,20)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values);

        // then
        assertEquals(15, average.doubleValue(), 0.01);
    }

    @Test
    void macd() {
        // given
        List<FileOhlcv> fileOhlcvs = loadTestOhlcvFile();

        // when
        Tool tool = new Tool();
        List<Macd> macds = tool.macd(fileOhlcvs.stream()
                .map(e->(Ohlcv)e)
                .collect(Collectors.toList()), 12, 26, 9);

        // then
        for(int i = 0, size = fileOhlcvs.size(); i < size; i ++) {
            FileOhlcv fileOhlcv = fileOhlcvs.get(i);
            Macd fileMacd = fileOhlcv.getMacd();
            Macd macd = macds.get(i);

            // 후반 데이터는 데이터 부족으로 불일치함.
            if(i < size - (26*4)) {
                log.debug("[{}] - {},{},{} | {},{},{}",
                        i,
                        fileMacd.getValue(), fileMacd.getSignal(), fileMacd.getOscillator(),
                        macd.getValue(), macd.getSignal(), macd.getOscillator());
                assertEquals(fileMacd.getValue().doubleValue(), macd.getValue().doubleValue(), 0.2);
                assertEquals(fileMacd.getSignal().doubleValue(), macd.getSignal().doubleValue(), 0.2);
                assertEquals(fileMacd.getOscillator().doubleValue(), macd.getOscillator().doubleValue(), 0.2);
            }
        }
    }

    @Test
    void dmi() {
        // given
        List<FileOhlcv> fileOhlcvs = loadTestOhlcvFile();

        // when
        Tool tool = new Tool();
        List<Dmi> dmis = tool.dmi(fileOhlcvs.stream()
                .map(e->(Ohlcv)e)
                .collect(Collectors.toList()), 14);

        // then
        for(int i = 0, size = fileOhlcvs.size(); i < size; i ++) {
            FileOhlcv fileOhlcv = fileOhlcvs.get(i);
            Dmi fileDmi = fileOhlcv.getDmi();
            Dmi dmi = dmis.get(i);

            // 후반 데이터는 데이터 부족으로 불일치함.
            if(i < size - (26*4)) {
                log.debug("[{}] - {},{},{} | {},{},{}",
                        i,
                        fileDmi.getPdi(), fileDmi.getMdi(), fileDmi.getAdx(),
                        dmi.getPdi(), dmi.getMdi(), dmi.getAdx());
                assertEquals(fileDmi.getPdi().doubleValue(), dmi.getPdi().doubleValue(), 0.2);
                assertEquals(fileDmi.getMdi().doubleValue(), dmi.getMdi().doubleValue(), 0.2);
                assertEquals(fileDmi.getAdx().doubleValue(), dmi.getAdx().doubleValue(), 0.2);
            }
        }
    }


}
