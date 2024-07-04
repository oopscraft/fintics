package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.asset.AssetOhlcvClient;
import org.oopscraft.fintics.client.asset.AssetOhlcvClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetOhlcv;
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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "asset-ohlcv-client.class-name", havingValue="org.oopscraft.fintics.client.asset.SimpleAssetOhlcvClient")
@Slf4j
public class SimpleAssetOhlcvClient extends AssetOhlcvClient {

    private final ObjectMapper objectMapper;

    public SimpleAssetOhlcvClient(AssetOhlcvClientProperties ohlcvClientProperties, ObjectMapper objectMapper) {
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
    public List<AssetOhlcv> getOhlcvs(Asset asset, AssetOhlcv.Type type, Instant datetimeFrom, Instant datetimeTo) {
        String yahooSymbol = convertToYahooSymbol(asset);
        return switch (type) {
            case MINUTE -> getMinuteOhlcvs(yahooSymbol, datetimeFrom, datetimeTo);
            case DAILY -> getDailyOhlcvs(yahooSymbol, datetimeFrom, datetimeTo);
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

    List<AssetOhlcv> getMinuteOhlcvs(String yahooSymbol, Instant datetimeFrom, Instant datetimeTo) {
        int validDays = 29;
        // check date time to
        if(datetimeTo.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1m";
        Instant period2 = datetimeTo.truncatedTo(ChronoUnit.MINUTES);
        Instant period1;
        Map<Instant, AssetOhlcv> minuteOhlcvMap = new LinkedHashMap<>();
        for (int i = 0; i < 10; i ++) {
            // period1
            period1 = period2.minus(7, ChronoUnit.DAYS);
            if (period1.isBefore(datetimeFrom)) {
                period1 = datetimeFrom.truncatedTo(ChronoUnit.MINUTES);
            }
            if (period1.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
                period1 = Instant.now().minus(validDays, ChronoUnit.DAYS);
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
            Map<Instant, AssetOhlcv> ohlcvMap = convertRootNodeToOhlcv(AssetOhlcv.Type.MINUTE, rootNode);
            minuteOhlcvMap.putAll(ohlcvMap);

            // next period2 and check break
            period2 = period1.minus(1, ChronoUnit.MINUTES);
            if (period2.isBefore(datetimeFrom)) {
                break;
            }
            if (period2.isBefore(Instant.now().minus(validDays, ChronoUnit.DAYS))) {
                break;
            }
        }

        // check date time is in range (holiday is not matched)
        List<AssetOhlcv> minuteOhlcvs = minuteOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    Instant datetime = ohlcv.getDatetime();
                    return (datetime.isAfter(datetimeFrom) || datetime.equals(datetimeFrom))
                            && (datetime.isBefore(datetimeTo) || datetime.equals(datetimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        minuteOhlcvs.sort(Comparator
                .comparing(AssetOhlcv::getDatetime)
                .reversed());
        // return
        return minuteOhlcvs;
    }

    List<AssetOhlcv> getDailyOhlcvs(String yahooSymbol, Instant datetimeFrom, Instant datetimeTo) {
        // check date time to
        Instant validDatetime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusYears(1)
                .toInstant(ZoneOffset.UTC);
        if(datetimeTo.isBefore(validDatetime)) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        HttpHeaders headers = createYahooHeader();
        String interval = "1d";
        Instant period1 = datetimeFrom.truncatedTo(ChronoUnit.DAYS);
        Instant period2 = Instant.now().truncatedTo(ChronoUnit.DAYS);
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
        Map<Instant, AssetOhlcv> dailyOhlcvMap = convertRootNodeToOhlcv(AssetOhlcv.Type.DAILY, rootNode);

        // check date time is in range (holiday is not matched)
        List<AssetOhlcv> dailyOhlcvs = dailyOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    Instant datetime = ohlcv.getDatetime();
                    return (datetime.isAfter(datetimeFrom) || datetime.equals(datetimeFrom))
                            && (datetime.isBefore(datetimeTo) || datetime.equals(datetimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        dailyOhlcvs.sort(Comparator
                .comparing(AssetOhlcv::getDatetime)
                .reversed());
        // return
        return dailyOhlcvs;
    }

    Map<Instant, AssetOhlcv> convertRootNodeToOhlcv(AssetOhlcv.Type type, JsonNode rootNode) {
        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        List<Long> timestamps = objectMapper.convertValue(resultNode.path("timestamp"), new TypeReference<>(){});
        JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
        List<BigDecimal> opens = objectMapper.convertValue(quoteNode.path("open"), new TypeReference<>(){});
        List<BigDecimal> highs = objectMapper.convertValue(quoteNode.path("high"), new TypeReference<>(){});
        List<BigDecimal> lows = objectMapper.convertValue(quoteNode.path("low"), new TypeReference<>(){});
        List<BigDecimal> closes = objectMapper.convertValue(quoteNode.path("close"), new TypeReference<>(){});
        List<BigDecimal> volumes = objectMapper.convertValue(quoteNode.path("volume"), new TypeReference<>(){});

        // duplicated data retrieved.
        Map<Instant, AssetOhlcv> ohlcvMap = new LinkedHashMap<>();
        if(timestamps != null) {        // if data not found, timestamps element is null.
            for(int i = 0; i < timestamps.size(); i ++) {
                Instant datetime = Instant.ofEpochSecond(timestamps.get(i));
                // truncates dateTime
                datetime = switch(type) {
                    case MINUTE -> datetime.truncatedTo(ChronoUnit.MINUTES);
                    case DAILY -> datetime.truncatedTo(ChronoUnit.DAYS);
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
                AssetOhlcv ohlcv = AssetOhlcv.builder()
                        .datetime(datetime)
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
