package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import com.github.javaparser.utils.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class TradeAssetDeciderTest {

    Trade getTestTrade() {
        return Trade.builder()
                .tradeId("test")
                .build();
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .tradeId("test")
                .assetName("Test")
                .build();
    }

    OrderBook getTestOrderBook() {
        return OrderBook.builder()
                .price(BigDecimal.valueOf(10000))
                .bidPrice(BigDecimal.valueOf(9990))
                .askPrice(BigDecimal.valueOf(10010))
                .build();
    }

    List<IndiceIndicator> getTestIndiceIndicators() {
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceId symbol : IndiceId.values()) {
            indiceIndicators.add(IndiceIndicator.builder()
                    .indiceId(symbol)
                    .minuteOhlcvs(new ArrayList<Ohlcv>(){{
                        add(Ohlcv.builder()
                                .dateTime(LocalDateTime.now())
                                .openPrice(BigDecimal.TEN)
                                .highPrice(BigDecimal.TEN)
                                .lowPrice(BigDecimal.TEN)
                                .closePrice(BigDecimal.TEN)
                                .volume(BigDecimal.TEN)
                                .build());
                    }})
                    .dailyOhlcvs(new ArrayList<Ohlcv>(){{
                        add(Ohlcv.builder()
                                .dateTime(LocalDate.now().atTime(0,0,0))
                                .openPrice(BigDecimal.TEN)
                                .highPrice(BigDecimal.TEN)
                                .lowPrice(BigDecimal.TEN)
                                .closePrice(BigDecimal.TEN)
                                .volume(BigDecimal.TEN)
                                .build());
                    }})
                    .build());
        }
        return indiceIndicators;
    }

    String loadGroovyFileAsString(String fileName) {
        String filePath = "org/oopscraft/fintics/trade/" + fileName;
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

    AssetIndicator getTestAssetIndicator(TradeAsset tradeAsset) {
        return AssetIndicator.builder()
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .minuteOhlcvs(IntStream.range(1,501)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusMinutes(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .volume(BigDecimal.valueOf(100))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .dailyOhlcvs(IntStream.range(1,301)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusDays(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .volume(BigDecimal.valueOf(10000))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    @Test
    void test() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        OrderBook orderBook = getTestOrderBook();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        trade.setHoldCondition("return true;");

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .log((Logger)log)
                .dateTime(LocalDateTime.now())
                .orderBook(orderBook)
                .indiceIndicators(indiceIndicators)
                .assetIndicator(assetIndicator)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
        assertTrue(result);
    }

    @Test
    void testHoldCondition() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        String holdCondition = loadGroovyFileAsString("HoldCondition.groovy");
        trade.setHoldCondition(holdCondition);

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .log((Logger) log)
                .dateTime(LocalDateTime.now())
                .indiceIndicators(indiceIndicators)
                .assetIndicator(assetIndicator)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }

}