package org.oopscraft.fintics.client.ohlcv.alphavantage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client.class-name", havingValue="org.oopscraft.fintics.client.ohlcv.alphavantage.AlphavantageOhlcvClient")
@Slf4j
public class AlphavantageOhlcvCollector {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final AlphavantageOhlcvRepository alphavantageOhlcvRepository;

    private final String apikey;

    public AlphavantageOhlcvCollector(OhlcvClientProperties ohlcvClientProperties, AlphavantageOhlcvRepository alphavantageOhlcvRepository) {
        this.ohlcvClientProperties = ohlcvClientProperties;
        this.alphavantageOhlcvRepository = alphavantageOhlcvRepository;
        this.apikey = ohlcvClientProperties.getProperty("apikey").orElseThrow();
    }

//    void collect(String symbol, String interval, String month) {
//        RestTemplate restTemplate = RestTemplateBuilder.create()
//                .insecure(true)
//                .build();
//        String url = UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query/")
//                .queryParam("function", "TIME_SERIES_INTRADAY")
//                .queryParam("symbol", symbol)
//                .queryParam("interval", interval)
//                .queryParam("month", month)
//                .queryParam("outputsize", "full")
//                .queryParam("apikey", apikey)
//                .queryParam("datatype", "csv")
//                .build()
//                .toUriString();
//        RequestEntity<Void> requestEntity = RequestEntity
//                .get(url)
//                .build();
//        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
//        String responseBody = responseEntity.getBody();
//
//
//        CSVFormat format = CSVFormat.Builder.create()
//                .setDelimiter(",")
//                .setHeader("timestamp", "open", "high", "low", "close", "volume")
//                .setSkipHeaderRecord(true)
//                .build();
//        List<Map<String,String>> list = new ArrayList<>();
//        try (InputStream inputStream = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8))) {
//            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
//                    .forEach(record -> {
//                        LocalDateTime timestamp = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")
//                                .parse(record.get("timestamp"));
//                        AlphavantageOhlcvEntity alphavantageOhlcvEntity = AlphavantageOhlcvEntity.builder()
//                                .timestamp(timestamp)
//                                .open(new BigDecimal(record.get("open")))
//                                .high(new BigDecimal(record.get("high")))
//                                .low(new BigDecimal(record.get("low")))
//                                .close(new BigDecimal(record.get("close")))
//                                .volume(new BigDecimal(record.get("volume")))
//                                .build();
//                        AlphavantageOhlcvEntity.Pk pk = AlphavantageOhlcvEntity.Pk.builder()
//                                        .build();
//                        if (!alphavantageOhlcvRepository.existsById(pk)) {
//
//                        }
//
//
//                        Map<String,String> map = new LinkedHashMap<>();
//                        for(String columnName : columnNames) {
//                            String columnValue = record.get(columnName);
//                            map.put(columnName, columnValue);
//                        }
//                        list.add(map);
//                    });
//        } catch(Throwable e) {
//            throw new RuntimeException(e);
//        }
//
//
//        System.out.println(responseBody);
//    }

}
