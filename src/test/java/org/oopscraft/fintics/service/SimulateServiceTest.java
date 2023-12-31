package org.oopscraft.fintics.service;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
import org.oopscraft.fintics.model.Simulate;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimulateServiceTest extends CoreTestSupport {

    private final SimulateService simulateService;

    private String loadHoldCondition(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    private List<Ohlcv> loadOhlcvs(String filePath, OhlcvType ohlcvType) {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("dateTime","open","high","low","close","volume")
                .setSkipHeaderRecord(true)
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            return CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .map(record -> Ohlcv.builder()
                            .ohlcvType(ohlcvType)
                            .dateTime(LocalDateTime.parse(record.get("dateTime"),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                            .openPrice(new BigDecimal(record.get("open").replaceAll(",","")))
                            .highPrice(new BigDecimal(record.get("high").replaceAll(",","")))
                            .lowPrice(new BigDecimal(record.get("low").replaceAll(",","")))
                            .closePrice(new BigDecimal(record.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(record.get("volume").replaceAll(",","")))
                            .build())
                    .collect(Collectors.toList());
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void simulate(String holdConditionFilePath, String minuteOhlcvFilePath, String dailyOhlcvFilePath, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        // given
        String holdCondition = loadHoldCondition(holdConditionFilePath);
        List<Ohlcv> minuteOhlcvs = loadOhlcvs(minuteOhlcvFilePath, OhlcvType.MINUTE);
        List<Ohlcv> dailyOhlcvs = loadOhlcvs(dailyOhlcvFilePath, OhlcvType.DAILY);

        // when
        Simulate simulate = Simulate.builder()
                .holdCondition(holdCondition)
                .startAt(LocalTime.of(9,0))
                .endAt(LocalTime.of(15,30))
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .feeRate(0.02)
                .bidAskSpread(5.0)
                .dateTimeFrom(dateTimeFrom)
                .dateTimeTo(dateTimeTo)
                .investAmount(BigDecimal.valueOf(10_000_000))
                .build();
        simulate = simulateService.simulate(simulate);

        // then
        log.warn("{}", simulate.getBalance());
        simulate.getOrders().forEach(el -> log.warn("{}", el));
    }

    @Disabled
    @Test
    void simulate_202311_KODEX레버리지() {
        simulate(
                "org/oopscraft/fintics/trade/HoldCondition.KospiCall.groovy",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX레버리지.minuteOhlcvs.tsv",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX레버리지.dailyOhlcvs.tsv",
                LocalDateTime.of(2023,11,21,0,0,0),
                LocalDateTime.of(2023,11,30,23,59,59)
        );
    }

    @Disabled
    @Test
    void simulate_202311_KODEX코스닥150레버리지() {
        simulate(
                "org/oopscraft/fintics/trade/HoldCondition.KospiCall.groovy",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX코스닥150레버리지.minuteOhlcvs.tsv",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX코스닥150레버리지.dailyOhlcvs.tsv",
                LocalDateTime.of(2023,11,21,0,0,0),
                LocalDateTime.of(2023,11,30,23,59,59)
        );
    }

    @Disabled
    @Test
    void simulate_202311_KODEX코스닥150선물인버스() {
        simulate(
                "org/oopscraft/fintics/trade/HoldCondition.KospiPut.groovy",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX코스닥150선물인버스.minuteOhlcvs.tsv",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX코스닥150선물인버스.dailyOhlcvs.tsv",
                LocalDateTime.of(2023,11,21,0,0,0),
                LocalDateTime.of(2023,11,30,23,59,59)
        );
    }

    @Disabled
    @Test
    void simulate_202311_KODEX200선물인버스2X() {
        simulate(
                "org/oopscraft/fintics/trade/HoldConditionKospiPut.groovy",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX200선물인버스2X.minuteOhlcvs.tsv",
                "org/oopscraft/fintics/service/SimulateServiceTest.202311.KODEX200선물인버스2X.dailyOhlcvs.tsv",
                LocalDateTime.of(2023,11,21,0,0,0),
                LocalDateTime.of(2023,11,30,23,59,59)
        );
    }




//    @Disabled
//    @Test
//    void simulate_20231030_KODEX코스닥150() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.call.groovy",
//                "SimulateServiceTest.20231030_KODEX코스닥150.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231030_KODEX코스닥150.dailyOhlcv.tsv");
//    }
//
//    @Disabled
//    @Test
//    void simulate_20231030_KODEX코스닥150선물인버스() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.put.groovy",
//                "SimulateServiceTest.20231030_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231030_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
//    }
//
//    @Disabled
//    @Test
//    void simulate_20231031_KODEX코스닥150() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.call.groovy",
//                "SimulateServiceTest.20231031_KODEX코스닥150.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231031_KODEX코스닥150.dailyOhlcv.tsv");
//    }
//
//    @Disabled
//    @Test
//    void simulate_20231031_KODEX코스닥150선물인버스() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.put.groovy",
//                "SimulateServiceTest.20231031_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231031_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
//    }
//
//    @Disabled
//    @Test
//    void simulate_20231101_KODEX코스닥150() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.call.groovy",
//                "SimulateServiceTest.20231101_KODEX코스닥150.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231101_KODEX코스닥150.dailyOhlcv.tsv");
//    }
//
//    @Disabled
//    @Test
//    void simulate_20231101_KODEX코스닥150선물인버스() throws Exception {
//        simulate("SimulateServiceTest.holdCondition.put.groovy",
//                "SimulateServiceTest.20231101_KODEX코스닥150선물인버스.minuteOhlcv.tsv",
//                "SimulateServiceTest.20231101_KODEX코스닥150선물인버스.dailyOhlcv.tsv");
//    }

}