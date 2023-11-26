package org.oopscraft.fintics.trade;

import com.mitchtalmadge.asciidata.graph.ASCIIGraph;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator._legacy.Dmi;
import org.oopscraft.fintics.calculator._legacy.Macd;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    @Test
    void testChart() {
        // given
        List<BigDecimal> rows = new ArrayList<>();
        for(int i = 0; i < 100; i ++) {
            rows.add(BigDecimal.valueOf(100*i));
        }

        // when
        Tool tool = new Tool();
        log.info(tool.plot(rows));
    }


//
//    private List<FileOhlcv> loadTestOhlcvFile() {
//        String filePath = "org/oopscraft/fintics/trade/ToolTest.tsv";
//        CSVFormat format = CSVFormat.Builder.create()
//                .setDelimiter("\t")
//                .setHeader("time","open","high","low","close","MACD","MACD-Signal","MACD-Oscillator", "RSI", "RSI-Signal", "ADX", "PDI", "MDI")
//                .setSkipHeaderRecord(true)
//                .build();
//        final List<FileOhlcv> rows = new ArrayList<>();
//        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
//            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
//                    .forEach(record -> {
//                        FileOhlcv row = FileOhlcv.builder()
//                                .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
//                                .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
//                                .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
//                                .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
//                                .build();
//                        row.setMacd(Macd.builder()
//                                .value(new BigDecimal(record.get("MACD").replaceAll(",","")))
//                                .signal(new BigDecimal(record.get("MACD-Signal").replaceAll(",","")))
//                                .oscillator(new BigDecimal(record.get("MACD-Oscillator").replaceAll(",","")))
//                                .build());
//                        row.setRsi(new BigDecimal(record.get("RSI").replaceAll("[,%]","")));
//                        row.setDmi(Dmi.builder()
//                                .pdi(new BigDecimal(record.get("PDI").replaceAll("[,%]","")))
//                                .mdi(new BigDecimal(record.get("MDI").replaceAll("[,%]","")))
//                                .adx(new BigDecimal(record.get("ADX").replaceAll("[,%]", "")))
//                                .build());
//                        rows.add(row);
//                    });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return rows;
//    }
//
//    @Test
//    void resample() {
//        // given
//        int size = 10;
//        LocalDateTime now = LocalDateTime.now();
//        List<Ohlcv> ohlcvs = IntStream.rangeClosed(1, 25)
//                .mapToObj(i -> Ohlcv.builder()
//                        .ohlcvType(OhlcvType.MINUTE)
//                        .dateTime(now.minusMinutes(i))
//                        .openPrice(BigDecimal.valueOf(100 + i))
//                        .highPrice(BigDecimal.valueOf(100 + i))
//                        .lowPrice(BigDecimal.valueOf(100 + i))
//                        .closePrice(BigDecimal.valueOf(100 + i))
//                        .volume(BigDecimal.TEN)
//                        .build())
//                .collect(Collectors.toList());
//
//        // when
//        Tool tool = new Tool();
//        List<Ohlcv> resampledOhlcvs = tool.resample(ohlcvs, size);
//
//        // then
//        log.info("== resampledOhlcvs:{}", resampledOhlcvs);
//        assertEquals(Math.round(IntStream.rangeClosed(101,110).average().orElse(0.0)), resampledOhlcvs.get(0).getOpenPrice().doubleValue());
//        assertEquals(Math.round(IntStream.rangeClosed(111,120).average().orElse(0.0)), resampledOhlcvs.get(1).getOpenPrice().doubleValue());
//        assertEquals(Math.round(IntStream.rangeClosed(121,125).average().orElse(0.0)), resampledOhlcvs.get(2).getOpenPrice().doubleValue());
//        assertEquals(100, resampledOhlcvs.get(0).getVolume().doubleValue());
//        assertEquals(100, resampledOhlcvs.get(1).getVolume().doubleValue());
//        assertEquals(50, resampledOhlcvs.get(2).getVolume().doubleValue());
//    }
//
//    @Test
//    @Order(1)
//    void mean() {
//        // given
//        List<BigDecimal> values = Stream.of(10,20)
//                .map(BigDecimal::valueOf)
//                .toList();
//
//        // when
//        Tool tool = new Tool();
//        BigDecimal average = tool.mean(values);
//
//        // then
//        assertEquals(15, average.doubleValue(), 0.01);
//    }
//
//    @Test
//    void macd() {
//        // given
//        List<FileOhlcv> fileOhlcvs = loadTestOhlcvFile();
//
//        // when
//        Tool tool = new Tool();
//        List<Macd> macds = tool.macd(fileOhlcvs.stream()
//                .map(e->(Ohlcv)e)
//                .collect(Collectors.toList()), 12, 26, 9);
//
//        // then
//        for(int i = 0, size = fileOhlcvs.size(); i < size; i ++) {
//            FileOhlcv fileOhlcv = fileOhlcvs.get(i);
//            Macd fileMacd = fileOhlcv.getMacd();
//            Macd macd = macds.get(i);
//
//            // 후반 데이터는 데이터 부족으로 불일치함.
//            if(i < size - (26*4)) {
//                log.debug("[{}] - {},{},{} | {},{},{}",
//                        i,
//                        fileMacd.getValue(), fileMacd.getSignal(), fileMacd.getOscillator(),
//                        macd.getValue(), macd.getSignal(), macd.getOscillator());
//                assertEquals(fileMacd.getValue().doubleValue(), macd.getValue().doubleValue(), 0.2);
//                assertEquals(fileMacd.getSignal().doubleValue(), macd.getSignal().doubleValue(), 0.2);
//                assertEquals(fileMacd.getOscillator().doubleValue(), macd.getOscillator().doubleValue(), 0.2);
//            }
//        }
//    }
//
//    @Test
//    void dmi() {
//        // given
//        List<FileOhlcv> fileOhlcvs = loadTestOhlcvFile();
//
//        // when
//        Tool tool = new Tool();
//        List<Dmi> dmis = tool.dmi(fileOhlcvs.stream()
//                .map(e->(Ohlcv)e)
//                .collect(Collectors.toList()), 14);
//
//        // then
//        for(int i = 0, size = fileOhlcvs.size(); i < size; i ++) {
//            FileOhlcv fileOhlcv = fileOhlcvs.get(i);
//            Dmi fileDmi = fileOhlcv.getDmi();
//            Dmi dmi = dmis.get(i);
//
//            // 후반 데이터는 데이터 부족으로 불일치함.
//            if(i < size - (26*4)) {
//                log.debug("[{}] - {},{},{} | {},{},{}",
//                        i,
//                        fileDmi.getPdi(), fileDmi.getMdi(), fileDmi.getAdx(),
//                        dmi.getPdi(), dmi.getMdi(), dmi.getAdx());
//                assertEquals(fileDmi.getPdi().doubleValue(), dmi.getPdi().doubleValue(), 0.2);
//                assertEquals(fileDmi.getMdi().doubleValue(), dmi.getMdi().doubleValue(), 0.2);
//                assertEquals(fileDmi.getAdx().doubleValue(), dmi.getAdx().doubleValue(), 0.2);
//            }
//        }
//    }



}
