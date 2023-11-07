package org.oopscraft.fintics.client.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.MarketIndicator;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@ConditionalOnProperty(prefix = "fintics.client.market", name = "class-name", havingValue="org.oopscraft.fintics.client.market.YahooMarketClient")
@RequiredArgsConstructor
@Slf4j
public class YahooMarketClient implements MarketClient {

    private final ObjectMapper objectMapper;

    @Override
    public MarketIndicator getNdxIndicator() {
        return getMarketIndex("^NDX", "NASDAQ 100");
    }

    @Override
    public MarketIndicator getNdxFutureIndicator() {
        return getMarketIndex("NQ=F", "Nasdaq Futures");
    }

    @Override
    public MarketIndicator getSpxIndicator() {
        return getMarketIndex("^GSPC", "S&P 500");
    }

    @Override
    public MarketIndicator getSpxFutureIndicator() {
        return getMarketIndex("ES=F", "S&P 500 Future");
    }

    @Override
    public MarketIndicator getDjiIndicator() {
        return getMarketIndex("^DJI", "Dow Jones Industrial Average");
    }

    @Override
    public MarketIndicator getDjiFutureIndicator() {
        return getMarketIndex("YM=F", "Dow Futures");
    }

    MarketIndicator getMarketIndex(String symbol, String name) {
        List<Ohlcv> minuteOhlcvs = getOhlcvs(symbol, "1m", LocalDateTime.now().minusWeeks(1), LocalDateTime.now(), 60*24*3);    // 3 days
        List<Ohlcv> dailyOhlcvs = getOhlcvs(symbol, "1d", LocalDateTime.now().minusMonths(3), LocalDateTime.now(), 30*2);       // 2 months
        return MarketIndicator.builder()
                .name(name)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build();
    }

    List<Ohlcv> getOhlcvs(String symbol, String interval, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Integer limit) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", symbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol",symbol)
                .queryParam("interval", interval)
                .queryParam("period1", dateTimeFrom.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("period2", dateTimeTo.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("corsDomain", "finance.yahoo.com")
                .build()
                .toUriString();

        HttpHeaders headers = createYahooHeader();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<Map<String,Object>> results = objectMapper.convertValue(rootNode.path("chart").path("result"), new TypeReference<>(){});
        if(results == null || results.isEmpty()) {
            throw new NoSuchElementException();
        }
        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        List<Long> timestamps = objectMapper.convertValue(resultNode.path("timestamp"), new TypeReference<>(){});
        JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
        List<BigDecimal> opens = objectMapper.convertValue(quoteNode.path("open"), new TypeReference<>(){});
        List<BigDecimal> highs = objectMapper.convertValue(quoteNode.path("high"), new TypeReference<>(){});
        List<BigDecimal> lows = objectMapper.convertValue(quoteNode.path("low"), new TypeReference<>(){});
        List<BigDecimal> closes = objectMapper.convertValue(quoteNode.path("close"), new TypeReference<>(){});
        List<BigDecimal> volumes = objectMapper.convertValue(quoteNode.path("volume"), new TypeReference<>(){});

        List<Ohlcv> ohlcvs = new ArrayList<>();
        for(int i = 0; i < timestamps.size(); i ++) {
            LocalDateTime dateTime = Instant.ofEpochSecond(timestamps.get(i))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            BigDecimal openPrice = opens.get(i);
            if(openPrice == null) {     // sometimes open price is null (data error)
                continue;
            }
            BigDecimal highPrice = Optional.ofNullable(highs.get(i)).orElse(openPrice);
            BigDecimal lowPrice = Optional.ofNullable(lows.get(i)).orElse(openPrice);
            BigDecimal closePrice = Optional.ofNullable(closes.get(i)).orElse(openPrice);
            BigDecimal volume = Optional.ofNullable(volumes.get(i)).orElse(BigDecimal.ZERO);
            Ohlcv ohlcv = Ohlcv.builder()
                    .dateTime(dateTime)
                    .openPrice(openPrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .closePrice(closePrice)
                    .volume(volume)
                    .build();
            ohlcvs.add(ohlcv);
        }
        Collections.reverse(ohlcvs);
        return ohlcvs.subList(0, Math.min(limit, ohlcvs.size()));
    }

    private HttpHeaders createYahooHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority"," query1.finance.yahoo.com");
        headers.add("Accept", "*/*");
        headers.add("origin", "https://finance.yahoo.com");
        headers.add("referer", "");
        headers.add("Sec-Ch-Ua","\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"");
        headers.add("Sec-Ch-Ua-Mobile","?0");
        headers.add("Sec-Ch-Ua-Platform", "macOS");
        headers.add("Sec-Fetch-Dest","document");
        headers.add("Sec-Fetch-Mode","navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }


}
