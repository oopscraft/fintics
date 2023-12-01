package org.oopscraft.fintics.service;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Simulate;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimulateServiceTest extends CoreTestSupport {

    private final SimulateService simulateService;

    private String loadHoldCondition(String fileName) throws Exception {
        String filePath = "org/oopscraft/fintics/service/" + fileName;
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        }
        return stringBuilder.toString();
    }

    private List<Ohlcv> loadDailyOhlcvs(String fileName) {
        String filePath = "org/oopscraft/fintics/service/" + fileName;
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("dateTime","open","high","low","close")
                .setSkipHeaderRecord(true)
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            return CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .map(record -> Ohlcv.builder()
                                .dateTime(LocalDate.parse(record.get("dateTime"), DateTimeFormatter.ofPattern("yyyy/MM/dd")).atTime(0,0))
                                .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
                                .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
                                .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
                                .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
                                .build())
                    .collect(Collectors.toList());
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private List<Ohlcv> loadMinuteOhlcvs(String fileName) {
        String filePath = "org/oopscraft/fintics/service/" + fileName;
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("dateTime","open","high","low","close")
                .setSkipHeaderRecord(true)
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            return CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .map(record -> Ohlcv.builder()
                            .dateTime(LocalDate.of(1,1,1).atTime(LocalTime.parse(record.get("dateTime"),DateTimeFormatter.ofPattern("MM/dd,HH:mm"))))
                            .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
                            .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
                            .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
                            .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
                            .build())
                    .collect(Collectors.toList());
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void simulate(String holdConditionFileName, String minuteOhlcvFileName, String dailyOhlcvFileName) throws Exception {
        // given
        String holdCondition = loadHoldCondition(holdConditionFileName);
        List<Ohlcv> minuteOhlcvs = loadMinuteOhlcvs(minuteOhlcvFileName);
        List<Ohlcv> dailyOhlcvs = loadDailyOhlcvs(dailyOhlcvFileName);
        Integer interval = 30;

        // when
        Simulate simulate = Simulate.builder()
                .holdCondition(holdCondition)
                .interval(interval)
                .startAt(LocalTime.of(9,0))
                .endAt(LocalTime.of(15,30))
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .feeRate(0.02)
                .bidAskSpread(5.0)
                .build();
        simulate = simulateService.simulate(simulate);

        // then
        log.info("{}", simulate.getHoldConditionResults());
    }

    @Disabled
    @Test
    void simulate_20231030_KODEX코스닥150() throws Exception {
        simulate("SimulateServiceTest.holdCondition.call.groovy",
                "SimulateServiceTest.20231030_KODEX코스닥150.minuteOhlcv.tsv",
                "SimulateServiceTest.20231030_KODEX코스닥150.dailyOhlcv.tsv");
    }

    @Disabled
    @Test
    void simulate_20231030_KODEX코스닥150선물인버스() throws Exception {
        simulate("SimulateServiceTest.holdCondition.put.groovy",
                "SimulateServiceTest.20231030_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
                "SimulateServiceTest.20231030_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
    }

    @Disabled
    @Test
    void simulate_20231031_KODEX코스닥150() throws Exception {
        simulate("SimulateServiceTest.holdCondition.call.groovy",
                "SimulateServiceTest.20231031_KODEX코스닥150.minuteOhlcv.tsv",
                "SimulateServiceTest.20231031_KODEX코스닥150.dailyOhlcv.tsv");
    }

    @Disabled
    @Test
    void simulate_20231031_KODEX코스닥150선물인버스() throws Exception {
        simulate("SimulateServiceTest.holdCondition.put.groovy",
                "SimulateServiceTest.20231031_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
                "SimulateServiceTest.20231031_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
    }

    @Disabled
    @Test
    void simulate_20231101_KODEX코스닥150() throws Exception {
        simulate("SimulateServiceTest.holdCondition.call.groovy",
                "SimulateServiceTest.20231101_KODEX코스닥150.minuteOhlcv.tsv",
                "SimulateServiceTest.20231101_KODEX코스닥150.dailyOhlcv.tsv");
    }

    @Disabled
    @Test
    void simulate_20231101_KODEX코스닥150선물인버스() throws Exception {
        simulate("SimulateServiceTest.holdCondition.put.groovy",
                "SimulateServiceTest.20231101_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
                "SimulateServiceTest.20231101_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
    }

}