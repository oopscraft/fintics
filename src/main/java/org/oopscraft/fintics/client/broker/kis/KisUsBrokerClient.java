package org.oopscraft.fintics.client.broker.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.KrBrokerClient;
import org.oopscraft.fintics.client.broker.UsBrokerClient;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class KisUsBrokerClient extends UsBrokerClient {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    public KisUsBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");
        this.objectMapper = new ObjectMapper();
    }

    HttpHeaders createHeaders() throws InterruptedException {
        KisAccessToken accessToken = KisAccessTokenRegistry.getAccessToken(apiUrl, appKey, appSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        httpHeaders.add("authorization", "Bearer " + accessToken.getAccessToken());
        httpHeaders.add("appkey", appKey);
        httpHeaders.add("appsecret", appSecret);
        return httpHeaders;
    }

    private synchronized static void sleep() throws InterruptedException {
        Thread.sleep(300);
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    boolean isHoliday(LocalDateTime dateTime) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76950200");
        headers.add("custtype", "P");
        String excd = "NAS";    // asset.getExchange();
        String symb = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", excd)
                .queryParam("SYMB", symb)
                .queryParam("NMIN", "1")
                .queryParam("PINC", "1")
                .queryParam("NEXT", "")
                .queryParam("NREC", "120")
                .queryParam("FILL", "")
                .queryParam("KEYB", "")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }
        List<ValueMap> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});
        return output2.stream()
                .map(row -> {
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(
                            row.getString("kymd") + row.getString("khms"),
                            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    );
                    BigDecimal openPrice = row.getNumber("open");
                    BigDecimal highPrice = row.getNumber("high");
                    BigDecimal lowPrice = row.getNumber("low");
                    BigDecimal closePrice = row.getNumber("last");
                    BigDecimal volume = row.getNumber("evol");
                    return Ohlcv.builder()
                            .type(Ohlcv.Type.MINUTE)
                            .dateTime(ohlcvDateTime)
                            .openPrice(openPrice)
                            .highPrice(highPrice)
                            .lowPrice(lowPrice)
                            .closePrice(closePrice)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-daily-chartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST03030100");
        headers.add("custtype", "P");
        String fidCondMrktDivCode = "N";
        String fidInputIscd = asset.getSymbol();
        String fidInputDate1 = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fidInputDate2 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fidPeriodDivCode = "D";
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_DATE_1", fidInputDate1)
                .queryParam("FID_INPUT_DATE_2", fidInputDate2)
                .queryParam("FID_PERIOD_DIV_CODE", fidPeriodDivCode)
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }
        List<ValueMap> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});
        return output2.stream()
                .map(row -> {
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(row.getString("stck_bsop_date") + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    BigDecimal openPrice = row.getNumber("ovrs_nmix_oprc");
                    BigDecimal highPrice = row.getNumber("ovrs_nmix_hgpr");
                    BigDecimal lowPrice = row.getNumber("ovrs_nmix_lwpr");
                    BigDecimal closePrice = row.getNumber("ovrs_nmix_prpr");
                    BigDecimal volume = row.getNumber("acml_vol");
                    return Ohlcv.builder()
                            .type(Ohlcv.Type.DAILY)
                            .dateTime(ohlcvDateTime)
                            .openPrice(openPrice)
                            .highPrice(highPrice)
                            .lowPrice(lowPrice)
                            .closePrice(closePrice)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-asking-price";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76200100");
        String excd = "NAS";
        String symb = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH","")
                .queryParam("EXCD", excd)
                .queryParam("SYMB", symb)
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        ValueMap output1 = objectMapper.convertValue(rootNode.path("output1"), ValueMap.class);
        ValueMap output2 = objectMapper.convertValue(rootNode.path("output2"), ValueMap.class);

        BigDecimal price = output1.getNumber("last");
        BigDecimal bidPrice = output2.getNumber("bidp1");
        BigDecimal askPrice = output2.getNumber("askp1");

        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

}
