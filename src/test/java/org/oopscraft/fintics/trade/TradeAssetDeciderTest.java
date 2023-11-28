package org.oopscraft.fintics.trade;

import com.github.javaparser.utils.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
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
                .name("Test")
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
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            indiceIndicators.add(IndiceIndicator.builder()
                    .symbol(symbol)
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

    TradeAssetIndicator getTestAssetIndicator(TradeAsset tradeAsset) {
        return TradeAssetIndicator.builder()
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
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
        TradeAssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        trade.setHoldCondition("return true;");

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .orderBook(orderBook)
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
        assertTrue(result);
    }

    @Disabled
    @Test
    void testHoldConditionCryptocurrency() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        TradeAssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        String holdCondition = loadGroovyFileAsString("HoldCondition.Cryptocurrency.groovy");
        trade.setHoldCondition(holdCondition);

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }

    @Disabled
    @Test
    void testHoldConditionKospiCall() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        TradeAssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        String holdCondition = loadGroovyFileAsString("HoldCondition.KospiCall.groovy");
        trade.setHoldCondition(holdCondition);

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }

    @Disabled
    @Test
    void testHoldConditionKospiPut() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        TradeAssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        String holdCondition = loadGroovyFileAsString("HoldCondition.KospiPut.groovy");
        trade.setHoldCondition(holdCondition);

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }


}