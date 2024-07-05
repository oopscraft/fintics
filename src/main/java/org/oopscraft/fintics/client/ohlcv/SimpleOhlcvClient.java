package org.oopscraft.fintics.client.ohlcv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client.class-name", havingValue="org.oopscraft.fintics.client.ohlcv.SimpleOhlcvClient")
@Slf4j
public class SimpleOhlcvClient extends OhlcvClient {

    private final ObjectMapper objectMapper;

    public SimpleOhlcvClient(OhlcvClientProperties ohlcvClientProperties, ObjectMapper objectMapper) {
        super(ohlcvClientProperties);
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isSupported(Asset asset) {
        String yahooSymbol = convertToYahooSymbol(asset);
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
        JsonNode rootNode;
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
    public List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime datetimeFrom, LocalDateTime datetimeTo) {
        return switch (type) {
            case MINUTE -> getMinuteOhlcvs(asset, datetimeFrom, datetimeTo);
            case DAILY -> getDailyOhlcvs(asset, datetimeFrom, datetimeTo);
        };
    }

    ZoneId getTimezone(Asset asset) {
        String market = asset.getAssetId().split("\\.")[0];
        return switch(market) {
            case "US" -> ZoneId.of("America/New_York");
            case "KR" -> ZoneId.of("Asia/Seoul");
            default -> ZoneId.of("UTC");
        };
    }

    String convertToYahooSymbol(Asset asset) {
        String yahooSymbol;
        String exchange = Optional.ofNullable(asset.getExchange()).orElseThrow(() -> new RuntimeException("exchange is null"));
        switch(exchange) {
            case "XKRX" -> yahooSymbol = String.format("%s.KS", asset.getSymbol());
            case "XKOS" -> yahooSymbol = String.format("%s.KQ", asset.getSymbol());
            default -> yahooSymbol = asset.getSymbol();
        }
        return yahooSymbol;
    }

    List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime datetimeFrom, LocalDateTime datetimeTo) {
        // yahoo finance 의 경우 분봉은 30일 까지만 제공
        int validDays = 29;

        // check date time to
        ZoneId timezone = getTimezone(asset);
        Instant instantTo = datetimeTo.atZone(timezone).toInstant();
        Instant instantFrom = datetimeFrom.atZone(timezone).toInstant();

        // instantTo 가 유효 기간 이전 이면 empty array 반환
        if(instantTo.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
            return new ArrayList<>();
        }

        // yahoo symbol
        String yahooSymbol = convertToYahooSymbol(asset);

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1m";
        Instant period2 = instantTo.truncatedTo(ChronoUnit.MINUTES);    // start period to
        Instant period1;
        Map<LocalDateTime, Ohlcv> minuteOhlcvMap = new LinkedHashMap<>();
        for (int i = 0; i < 10; i ++) {
            // defines period1
            period1 = period2.minus(7, ChronoUnit.DAYS);
            if (period1.isBefore(instantFrom)) {
                period1 = instantFrom.truncatedTo(ChronoUnit.MINUTES);
            }
            if (period1.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
                period1 = Instant.now().minus(validDays, ChronoUnit.DAYS);
            }

            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("symbol", yahooSymbol)
                    .queryParam("interval", interval)
                    .queryParam("period1", period1.atZone(ZoneOffset.UTC).toEpochSecond())
                    .queryParam("period2", period2.atZone(ZoneOffset.UTC).toEpochSecond())
                    .queryParam("corsDomain", "finance.yahoo.com")
                    .build()
                    .toUriString();
            RequestEntity<Void> requestEntity = RequestEntity
                    .get(url)
                    .headers(headers)
                    .build();
            ResponseEntity<String> responseEntity = null;
            try {
                responseEntity = restTemplate.exchange(requestEntity, String.class);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    String responseBody = e.getResponseBodyAsString();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode errorNode = objectMapper.readTree(responseBody).path("error");
                        if (errorNode.isObject()) {
                            String code = errorNode.path("code").asText();
                            String description = errorNode.path("description").asText();
                            if ("Bad Request".equals(code) && description.contains("Data doesn't exist")) {
                                log.debug(description);
                                return new ArrayList<>();
                            }
                        }
                    } catch (JsonProcessingException jsonProcessingException) {
                        throw new RuntimeException("Failed to parse error response", jsonProcessingException);
                    }
                } else {
                    throw e;
                }
            }

            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(responseEntity.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            Map<LocalDateTime, Ohlcv> ohlcvMap = convertRootNodeToOhlcv(asset, Ohlcv.Type.MINUTE, rootNode);
            minuteOhlcvMap.putAll(ohlcvMap);

            // next period2 and check break
            period2 = period1.minus(1, ChronoUnit.MINUTES);
            if (period2.isBefore(instantFrom)) {
                break;
            }
            if (period2.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
                break;
            }
        }

        // check date time is in range (holiday is not matched)
        List<Ohlcv> minuteOhlcvs = minuteOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    LocalDateTime dateTime = ohlcv.getDateTime();
                    return (dateTime.isAfter(datetimeFrom) || dateTime.isEqual(datetimeFrom))
                            && (dateTime.isBefore(datetimeTo) || dateTime.isEqual(datetimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        minuteOhlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());
        // return
        return minuteOhlcvs;
    }

    List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime datetimeFrom, LocalDateTime datetimeTo) {
        // check date time to
        ZoneId timezone = getTimezone(asset);
        Instant instantFrom = datetimeFrom.atZone(timezone).toInstant();
        Instant instantTo = datetimeTo.atZone(timezone).toInstant();
        Instant validInstant = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusYears(1)
                .toInstant(ZoneOffset.UTC);
        if(instantTo.isBefore(validInstant)) {
            return new ArrayList<>();
        }

        // yahoo symbol
        String yahooSymbol = convertToYahooSymbol(asset);

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1d";
        Instant period1 = instantFrom.truncatedTo(ChronoUnit.DAYS);
        Instant period2 = instantTo.truncatedTo(ChronoUnit.DAYS);
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol", yahooSymbol)
                .queryParam("interval", interval)
                .queryParam("period1", period1.atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("period2", period2.atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("corsDomain", "finance.yahoo.com")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode errorNode = objectMapper.readTree(responseBody).path("error");
                    if (errorNode.isObject()) {
                        String code = errorNode.path("code").asText();
                        String description = errorNode.path("description").asText();
                        if ("Bad Request".equals(code) && description.contains("Data doesn't exist")) {
                            log.debug(description);
                            return new ArrayList<>();
                        }
                    }
                } catch (JsonProcessingException jsonProcessingException) {
                    throw new RuntimeException("Failed to parse error response", jsonProcessingException);
                }
            } else {
                throw e;
            }
        }
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<LocalDateTime, Ohlcv> dailyOhlcvMap = convertRootNodeToOhlcv(asset, Ohlcv.Type.DAILY, rootNode);

        // check date time is in range (holiday is not matched)
        List<Ohlcv> dailyOhlcvs = dailyOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    LocalDateTime dateTime = ohlcv.getDateTime();
                    return (dateTime.isAfter(datetimeFrom) || dateTime.isEqual(datetimeFrom))
                            && (dateTime.isBefore(datetimeTo) || dateTime.isEqual(datetimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        dailyOhlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());
        // return
        return dailyOhlcvs;
    }

    Map<LocalDateTime, Ohlcv> convertRootNodeToOhlcv(Asset asset, Ohlcv.Type type, JsonNode rootNode) {
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

                // truncates dateTime
                Instant instant = Instant.ofEpochSecond(timestamps.get(i));
                ZoneId timezone = getTimezone(asset);
                LocalDateTime datetime = switch(type) {
                    case MINUTE -> instant
                            .atZone(timezone)
                            .toLocalDateTime()
                            .truncatedTo(ChronoUnit.MINUTES);
                    case DAILY -> instant
                            .atZone(timezone)
                            .toLocalDateTime()
                            .truncatedTo(ChronoUnit.DAYS);
                };
                // ohlcv value
                BigDecimal open = opens.get(i);
                if(open == null) {     // sometimes open price is null (data error)
                    continue;
                }
                BigDecimal high = Optional.ofNullable(highs.get(i)).orElse(open);
                BigDecimal low = Optional.ofNullable(lows.get(i)).orElse(open);
                BigDecimal close = Optional.ofNullable(closes.get(i)).orElse(open);
                BigDecimal volume = Optional.ofNullable(volumes.get(i)).orElse(BigDecimal.ZERO);
                Ohlcv ohlcv = Ohlcv.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(datetime)
                        .timeZone(timezone)
                        .type(type)
                        .open(open.setScale(2, RoundingMode.HALF_UP))
                        .high(high.setScale(2, RoundingMode.HALF_UP))
                        .low(low.setScale(2, RoundingMode.HALF_UP))
                        .close(close.setScale(2, RoundingMode.HALF_UP))
                        .volume(volume.setScale(2, RoundingMode.HALF_UP))
                        .build();
                ohlcvMap.put(datetime, ohlcv);
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
