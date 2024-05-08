package org.oopscraft.fintics.client.ohlcv.alphavantage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client.class-name", havingValue="org.oopscraft.fintics.client.ohlcv.alphavantage.AlphavantageOhlcvClient")
@Slf4j
public class AlphavantageOhlcvClient extends OhlcvClient {

    private final ObjectMapper objectMapper;

    private final String apikey;

    public AlphavantageOhlcvClient(OhlcvClientProperties ohlcvClientProperties, ObjectMapper objectMapper) {
        super(ohlcvClientProperties);
        this.objectMapper = objectMapper;
        this.apikey = ohlcvClientProperties.getProperty("apikey").orElseThrow();
    }

    @Override
    public boolean isSupported(Asset asset) {
        return false;
    }

    @Override
    public boolean isSupported(Indice indice) {
        return false;
    }

    @Override
    public List<Ohlcv> getAssetOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> ohlcvs = new ArrayList<>();
        List<YearMonth> yearMonths = getYearMonthsBetween(dateTimeFrom, dateTimeTo);
        for (YearMonth yearMonth : yearMonths) {

        }
        return null;
    }

    @Override
    public List<Ohlcv> getIndiceOhlcvs(Indice indice, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        return null;
    }

    String getAlphavantageSymbol(Asset asset) {
        String market = asset.getMarket();
        String symbol = asset.getSymbol();
        String exchange = asset.getExchange();
        String alphavantageSymbol = null;
        switch (market) {
            case "KR" -> {
                alphavantageSymbol = ".KS"
            }
            case "US" -> {

            }
            default -> throw new RuntimeException("invalid market");
        }
    }

    List<YearMonth> getYearMonthsBetween(LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(dateTimeFrom);
        YearMonth endMonth = YearMonth.from(dateTimeTo);
        while (!startMonth.isAfter(endMonth)) {
            months.add(YearMonth.of(startMonth.getYear(), startMonth.getMonth()));
            startMonth = startMonth.plusMonths(1);
        }
        return months;
    }

    List<Ohlcv> getMinuteOhlcvs(String alphavantageSymbol, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> minuteOhlcvs = new ArrayList<>();
        List<YearMonth> yearMonths = getYearMonthsBetween(dateTimeFrom, dateTimeTo);
        for (YearMonth yearMonth : yearMonths) {
            minuteOhlcvs.addAll(getMinuteOhlcvsByYearMonth(alphavantageSymbol, yearMonth));
        }
        // filter
        return minuteOhlcvs.stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                        && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                .collect(Collectors.toList());
    }

    List<Ohlcv> getMinuteOhlcvsByYearMonth(String alphavantageSymbol, YearMonth yearMonth) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query/")
                .queryParam("function", "TIME_SERIES_INTRADAY")
                .queryParam("symbol", alphavantageSymbol)
                .queryParam("interval", "1min")
                .queryParam("month", yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .queryParam("outputsize", "full")
                .queryParam("apikey", apikey)
                .queryParam("datatype", "csv")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        return convertCsvStringToOhlcvs(responseBody, Ohlcv.Type.MINUTE);
    }

    List<Ohlcv> getDailyOhlcvs(String alphavantageSymbol) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query/")
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", alphavantageSymbol)
                .queryParam("outputsize", "full")
                .queryParam("apikey", apikey)
                .queryParam("datatype", "csv")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        return convertCsvStringToOhlcvs(responseBody, Ohlcv.Type.DAILY);
    }

    List<Ohlcv> convertCsvStringToOhlcvs(String csvString, Ohlcv.Type type) {
        List<Ohlcv> ohlcvs = new ArrayList<>();
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(",")
                .setHeader("timestamp", "open", "high", "low", "close", "volume")
                .setSkipHeaderRecord(true)
                .build();
        try (InputStream inputStream = new ByteArrayInputStream(csvString.getBytes(StandardCharsets.UTF_8))) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        String timestamp = record.get("timestamp");
                        LocalDateTime dateTime = null;
                        switch (type) {
                            case MINUTE -> {
                                dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            }
                            case DAILY -> {
                                dateTime = LocalDate.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(LocalTime.MIDNIGHT);
                            }
                            default -> throw new RuntimeException("invalid type");
                        }
                        Ohlcv ohlcv = Ohlcv.builder()
                                .dateTime(dateTime)
                                .type(type)
                                .openPrice(new BigDecimal(record.get("open")))
                                .highPrice(new BigDecimal(record.get("high")))
                                .lowPrice(new BigDecimal(record.get("low")))
                                .closePrice(new BigDecimal(record.get("close")))
                                .volume(new BigDecimal(record.get("volume")))
                                .build();
                        ohlcvs.add(ohlcv);
                    });
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return ohlcvs;
    }

}
