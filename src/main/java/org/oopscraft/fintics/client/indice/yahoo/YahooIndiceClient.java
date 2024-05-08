package org.oopscraft.fintics.client.indice.yahoo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.indice.IndiceClientProperties;
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
@ConditionalOnProperty(prefix = "fintics", name = "indice-client.class-name", havingValue="org.oopscraft.fintics.client.indice.yahoo.YahooIndiceClient")
@Slf4j
public class YahooIndiceClient extends IndiceClient {

    private final ObjectMapper objectMapper;

    protected YahooIndiceClient(IndiceClientProperties indiceClientProperties, ObjectMapper objectMapper) {
        super(indiceClientProperties);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Indice.Id indiceId, LocalDateTime dateTime) {
        return getOhlcvs(indiceId, Ohlcv.Type.MINUTE, dateTime.minusDays(3), dateTime, 60*24);    // 1 days
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Indice.Id indiceId, LocalDateTime dateTime) {
        return getOhlcvs(indiceId, Ohlcv.Type.DAILY, dateTime.minusMonths(2), dateTime, 30);    // 1 months
    }

    private String convertYahooSymbol(Indice.Id indiceId) {
        return switch(indiceId) {
            case NDX -> "^NDX";
            case NDX_FUTURE -> "NQ=F";
            case SPX -> "^GSPC";
            case SPX_FUTURE -> "ES=F";
            case KOSPI -> "^KS11";
            case USD_KRW -> "KRW=X";
            case BITCOIN -> "BTC-USD";
        };
    }

    private List<Ohlcv> getOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Integer limit) {
        String yahooSymbol = convertYahooSymbol(indiceId);
        String interval;
        switch(type) {
            case MINUTE -> interval = "1m";
            case DAILY -> interval = "1d";
            default -> throw new IllegalArgumentException("invalid ohlcvType");
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol",yahooSymbol)
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
        List<Ohlcv> ohlcvs = new ArrayList<>(ohlcvMap.values());

        // sort by dateTime(sometimes response is not ordered)
        ohlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());

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
