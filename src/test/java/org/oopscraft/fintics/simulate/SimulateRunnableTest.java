package org.oopscraft.fintics.simulate;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.OhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimulateRunnableTest extends CoreTestSupport {

    private final OhlcvRepository assetOhlcvRepository;

    private final ApplicationContext applicationContext;

    private final SimulateRunnableFactory simulateRunnableFactory;


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

    private List<Ohlcv> loadOhlcvs(String filePath) {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("type","datetime","open","high","low","close","volume")
                .setSkipHeaderRecord(true)
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            List<Ohlcv> ohlcvs = CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .map(record -> Ohlcv.builder()
                            .type(Ohlcv.Type.valueOf(record.get("type")))
                            .dateTime(LocalDateTime.parse(record.get("datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                            .open(new BigDecimal(record.get("open").replaceAll(",","")))
                            .high(new BigDecimal(record.get("high").replaceAll(",","")))
                            .low(new BigDecimal(record.get("low").replaceAll(",","")))
                            .close(new BigDecimal(record.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(record.get("volume").replaceAll(",","")))
                            .build())
                    .collect(Collectors.toList());
            Collections.reverse(ohlcvs);
            return ohlcvs;
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @Test
    void run() throws Exception {
        // given
        Trade trade = Trade.builder()
                .tradeId("test")
                .tradeName("Test Trade")
                .interval(60)
                .threshold(3)
                .startTime(LocalTime.of(9,0,0))
                .endTime(LocalTime.of(15,30,0))
                .orderKind(Order.Kind.MARKET)
                .build();
        List<TradeAsset> tradeAssets = new ArrayList<>();
        TradeAsset tradeAsset = TradeAsset.builder()
                .assetId("122630")
                .assetName("KODEX 레버리지")
                .enabled(true)
                .holdingWeight(BigDecimal.valueOf(30))
                .build();
        trade.getTradeAssets().add(tradeAsset);
        Strategy strategy = Strategy.builder()
                .script("return null")
                .build();
        Simulate simulate = Simulate.builder()
                .simulateId(IdGenerator.uuid())
                .trade(trade)
                .strategy(strategy)
                .investFrom(LocalDateTime.of(2023,12,4,0,0))
                .investTo(LocalDateTime.of(2023,12,4,23,59))
                .investAmount(BigDecimal.valueOf(10_000_000))
                .build();

        // when
        SimulateRunnable simulateRunnable = simulateRunnableFactory.getObject(simulate);
        simulateRunnable.run();

        // then
        Balance balance = simulate.getBalance();;
        List<Order> orders = simulate.getOrders();
        log.info("Balance:{}", balance);
        log.info("Orders:{}", orders);

    }

}