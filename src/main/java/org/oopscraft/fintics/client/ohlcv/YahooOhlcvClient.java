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
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client-class-name", havingValue="org.oopscraft.fintics.client.ohlcv.YahooOhlcvClient")
@RequiredArgsConstructor
@Slf4j
public class YahooOhlcvClient extends OhlcvClient {

    private final ObjectMapper objectMapper;

    @Override
    public boolean isSupported(Asset asset) {
        String yahooSymbol = convertToYahooSymbol(asset);
        return isSupported(yahooSymbol);
    }

    @Override
    public boolean isSupported(Indice indice) {
        String yahooSymbol = convertToYahooSymbol(indice.getIndiceId());
        return isSupported(yahooSymbol);
    }

    boolean isSupported(String yahooSymbol) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = String.format("https://query1.finance.yahoo.com/v1/finance/quoteType/?symbol=%s", yahooSymbol);
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode resultNode = rootNode.path("quoteType").path("result");
        for (JsonNode result : resultNode) {
            if (yahooSymbol.contentEquals(result.get("symbol").asText())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Ohlcv> getAssetOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        String yahooSymbol = convertToYahooSymbol(asset);
        return switch (type) {
            case MINUTE -> getMinuteOhlcvs(yahooSymbol, dateTimeFrom, dateTimeTo);
            case DAILY -> getDailyOhlcvs(yahooSymbol, dateTimeFrom, dateTimeTo);
        };
    }

    @Override
    public List<Ohlcv> getIndiceOhlcvs(Indice indice, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        String yahooSymbol = convertToYahooSymbol(indice.getIndiceId());
        return switch (type) {
            case MINUTE -> getMinuteOhlcvs(yahooSymbol, dateTimeFrom, dateTimeTo);
            case DAILY -> getDailyOhlcvs(yahooSymbol, dateTimeFrom, dateTimeTo);
        };
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
            default -> yahooSymbol = asset.getSymbol();
        }
        return yahooSymbol;
    }

    List<Ohlcv> getMinuteOhlcvs(String yahooSymbol, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        int validDays = 29;
        // check date time to
        if(dateTimeTo.isBefore(LocalDateTime.now().minusDays(validDays))) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1m";
        LocalDateTime period2 = dateTimeTo.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime period1;
        Map<LocalDateTime, Ohlcv> minuteOhlcvMap = new LinkedHashMap<>();
        for (int i = 0; i < 10; i ++) {
            // period1
            period1 = period2.minusDays(7);
            if (period1.isBefore(dateTimeFrom)) {
                period1 = dateTimeFrom.truncatedTo(ChronoUnit.MINUTES);
            }
            if (period1.isBefore(LocalDateTime.now().minusDays(validDays))) {
                period1 = LocalDateTime.now().minusDays(validDays);
            }

            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("symbol", yahooSymbol)
                    .queryParam("interval", interval)
                    .queryParam("period1", period1.atZone(ZoneId.systemDefault()).toEpochSecond())
                    .queryParam("period2", period2.atZone(ZoneId.systemDefault()).toEpochSecond())
                    .queryParam("corsDomain", "finance.yahoo.com")
                    .build()
                    .toUriString();
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
            Map<LocalDateTime, Ohlcv> ohlcvMap = convertRootNodeToOhlcv(Ohlcv.Type.MINUTE, rootNode);
            minuteOhlcvMap.putAll(ohlcvMap);

            // next period2 and check break
            period2 = period1.minusMinutes(1);
            if (period2.isBefore(dateTimeFrom)) {
                break;
            }
            if (period2.isBefore(LocalDateTime.now().minusDays(validDays))) {
                break;
            }
        }

        // check date time is in range (holiday is not matched)
        List<Ohlcv> minuteOhlcvs = minuteOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    LocalDateTime dateTime = ohlcv.getDateTime();
                    return (dateTime.isAfter(dateTimeFrom) || dateTime.isEqual(dateTimeFrom))
                            && (dateTime.isBefore(dateTimeTo) || dateTime.isEqual(dateTimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        minuteOhlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());
        // return
        return minuteOhlcvs;
    }

    List<Ohlcv> getDailyOhlcvs(String yahooSymbol, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        int validYears = 1;
        // check date time to
        if(dateTimeTo.isBefore(LocalDateTime.now().minusYears(validYears))) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1d";
        LocalDateTime period2 = dateTimeTo.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime period1 = dateTimeFrom.truncatedTo(ChronoUnit.DAYS);
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol", yahooSymbol)
                .queryParam("interval", interval)
                .queryParam("period1", period1.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("period2", period2.atZone(ZoneId.systemDefault()).toEpochSecond())
                .queryParam("corsDomain", "finance.yahoo.com")
                .build()
                .toUriString();
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
        Map<LocalDateTime, Ohlcv> dailyOhlcvMap = convertRootNodeToOhlcv(Ohlcv.Type.MINUTE, rootNode);

        // check date time is in range (holiday is not matched)
        List<Ohlcv> dailyOhlcvs = dailyOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    LocalDateTime dateTime = ohlcv.getDateTime();
                    return (dateTime.isAfter(dateTimeFrom) || dateTime.isEqual(dateTimeFrom))
                            && (dateTime.isBefore(dateTimeTo) || dateTime.isEqual(dateTimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        dailyOhlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());
        // return
        return dailyOhlcvs;
    }

    Map<LocalDateTime, Ohlcv> convertRootNodeToOhlcv(Ohlcv.Type type, JsonNode rootNode) {
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
            }
        }
        return ohlcvMap;
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
