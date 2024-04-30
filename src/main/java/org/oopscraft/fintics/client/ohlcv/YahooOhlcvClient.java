package org.oopscraft.fintics.client.ohlcv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client-class-name", havingValue="org.oopscraft.fintics.client.ohlcv.YahooOhlcvClient")
@RequiredArgsConstructor
@Slf4j
public class YahooOhlcvClient extends OhlcvClient {

    private final ObjectMapper objectMapper;

    @Override
    public List<Ohlcv> getAssetOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        String yahooSymbol = convertToYahooSymbol(asset);
        return getOhlcvs(yahooSymbol, type, dateTimeFrom, dateTimeTo);
    }

    @Override
    public List<Ohlcv> getIndiceOhlcvs(Indice indice, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        String yahooSymbol = convertToYahooSymbol(indice.getIndiceId());
        return getOhlcvs(yahooSymbol, type, dateTimeFrom, dateTimeTo);
    }

    String convertToYahooSymbol(Indice.Id indiceId) {
        String yahooSymbol = null;
        switch (indiceId) {
            case NDX -> yahooSymbol = "^NDX";
            case NDX_FUTURE -> yahooSymbol = "NQ=F";
            case SPX -> yahooSymbol = "^GSPC";
            case SPX_FUTURE -> yahooSymbol = "ES=F";
            case KOSPI -> yahooSymbol = "^KS11";
            case USD_KRW -> yahooSymbol = "KRW=X";
            case BITCOIN -> yahooSymbol = "BTC-USD";
        }
        return yahooSymbol;
    }

    String convertToYahooSymbol(Asset asset) {
        String yahooSymbol = null;
        String exchange = Optional.ofNullable(asset.getExchange()).orElseThrow(() -> new RuntimeException("exchange is null"));
        switch(exchange) {
            case "XKRX" -> yahooSymbol = String.format("%s.KS", asset.getSymbol());
            case "XKOS" -> yahooSymbol = String.format("%s.KQ", asset.getSymbol());
        }
        return yahooSymbol;
    }

    List<Ohlcv> getOhlcvs(String yahooSymbol, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();

        // url
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol", yahooSymbol)
                .queryParam("interval", "-")
                .queryParam("period1", dateTimeFrom.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("period2", dateTimeTo.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("corsDomain", "finance.yahoo.com")
                .build()
                .toUriString();

        // intervals
        List<String> intervals = null;
        switch(type) {
            case MINUTE -> intervals = List.of("1m","2m","5m","15m","30m","60m","90m");
            case DAILY -> intervals = List.of("1d","5d");
        }

        // try request
        String interval = null;
        ResponseEntity<String> responseEntity = null;
        for(int i = 0; i < intervals.size(); i ++) {
            interval = intervals.get(i);
            try {
                url = UriComponentsBuilder.fromUriString(url)
                        .replaceQueryParam("interval", interval)
                        .build()
                        .toUriString();
                RequestEntity<Void> requestEntity = RequestEntity
                        .get(url)
                        .headers(headers)
                        .build();
                responseEntity = restTemplate.exchange(requestEntity, String.class);
            } catch(Throwable e) {
                log.debug(e.getMessage());
                continue;
            }
            break;
        }

        // response body is null, return empty list
        if(responseEntity == null) {
            return new ArrayList<>();
        }

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, Object>> results = objectMapper.convertValue(rootNode.path("chart").path("result"), new TypeReference<>(){});
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

        // duplicated data retrieved.
        Map<LocalDateTime, Ohlcv> ohlcvMap = new LinkedHashMap<>();
        if(timestamps != null) {        // if data not found, timestamps element is null.
            for(int i = 0; i < timestamps.size(); i ++) {
                LocalDateTime dateTime = Instant.ofEpochSecond(timestamps.get(i))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                // truncates dateTime
                switch(type) {
                    case MINUTE -> dateTime = dateTime.truncatedTo(ChronoUnit.MINUTES);
                    case DAILY -> dateTime = dateTime.truncatedTo(ChronoUnit.DAYS);
                }

                // check date time is in range (holiday is not matched)
                boolean inRange = (dateTime.isAfter(dateTimeFrom) || dateTime.isEqual(dateTimeFrom))
                        && (dateTime.isBefore(dateTimeTo) || dateTime.isEqual(dateTimeTo));
                if(!inRange) {
                    continue;
                }

                // ohlcv value
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
                        .type(type)
                        .openPrice(openPrice.setScale(2, RoundingMode.HALF_UP))
                        .highPrice(highPrice.setScale(2, RoundingMode.HALF_UP))
                        .lowPrice(lowPrice.setScale(2, RoundingMode.HALF_UP))
                        .closePrice(closePrice.setScale(2, RoundingMode.HALF_UP))
                        .volume(volume.setScale(2, RoundingMode.HALF_UP))
                        .build();
                ohlcvMap.put(dateTime, ohlcv);

                // interpolates minute ohlcv
                if(type == Ohlcv.Type.MINUTE) {
                    int intervalMinutes = Integer.parseInt(interval.replace("m",""));
                    for(int j = 1; j < intervalMinutes; j++) {
                        LocalDateTime interpolatedDateTime = dateTime.plusMinutes(j);
                        Ohlcv interpolatedOhlcv = Ohlcv.builder()
                                .dateTime(interpolatedDateTime)
                                .type(type)
                                .openPrice(openPrice.setScale(2, RoundingMode.HALF_UP))
                                .highPrice(highPrice.setScale(2, RoundingMode.HALF_UP))
                                .lowPrice(lowPrice.setScale(2, RoundingMode.HALF_UP))
                                .closePrice(closePrice.setScale(2, RoundingMode.HALF_UP))
                                .volume(BigDecimal.ZERO)
                                .interpolated(true)
                                .build();
                        ohlcvMap.put(interpolatedDateTime, interpolatedOhlcv);
                    }
                }
                // interpolates daily ohlcv
                if(type == Ohlcv.Type.DAILY) {
                    int intervalDays = Integer.parseInt(interval.replace("d",""));
                    for(int j = 1; j < intervalDays; j++) {
                        LocalDateTime interpolatedDateTime = dateTime.plusDays(j);
                        Ohlcv interpolatedOhlcv = Ohlcv.builder()
                                .dateTime(interpolatedDateTime)
                                .type(type)
                                .openPrice(openPrice.setScale(2, RoundingMode.HALF_UP))
                                .highPrice(highPrice.setScale(2, RoundingMode.HALF_UP))
                                .lowPrice(lowPrice.setScale(2, RoundingMode.HALF_UP))
                                .closePrice(closePrice.setScale(2, RoundingMode.HALF_UP))
                                .volume(BigDecimal.ZERO)
                                .interpolated(true)
                                .build();
                        ohlcvMap.put(interpolatedDateTime, interpolatedOhlcv);
                    }
                }

            }
        }
        List<Ohlcv> ohlcvs = new ArrayList<>(ohlcvMap.values());

        // sort by dateTime(sometimes response is not ordered)
        ohlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());

        // return
        return ohlcvs;
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
