package org.oopscraft.fintics.client.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KisClient extends Client {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    private String accessToken;

    private LocalDateTime accessTokenExpiredDateTime;

    public KisClient(Properties properties) {
        super(properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");
        this.objectMapper = new ObjectMapper();

        // load access toke
        loadAccessToken();
    }

    static synchronized void sleep() {
        try {
            Thread.sleep(500);
        }catch(Throwable ignored){}
    }

    void loadAccessToken() {
        log.info("load access token");
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        ValueMap payloadMap = new ValueMap(){{
            put("grant_type","client_credentials");
            put("appkey", appKey);
            put("appsecret", appSecret);
        }};
        RequestEntity<Map<String,Object>> requestEntity = RequestEntity
                .post(apiUrl + "/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        this.accessToken = responseMap.getString("access_token");
        this.accessTokenExpiredDateTime = LocalDateTime.parse(
                responseMap.getString("access_token_token_expired"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    HttpHeaders createHeaders() {
        // if access token is after exited time(1 hour buffer)
        if(LocalDateTime.now().isAfter(accessTokenExpiredDateTime.minusHours(1))) {
            log.info("access token exceeded expired time");
            loadAccessToken();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        httpHeaders.add("authorization", "Bearer " + accessToken);
        httpHeaders.add("appkey", appKey);
        httpHeaders.add("appsecret", appSecret);
        return httpHeaders;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010200");
        String fidCondMrktDivCode = "J";
        String fidInputIscd;
        switch(asset.getType()) {
            case STOCK:
            case ETF:
                fidInputIscd = asset.getSymbol();
                break;
            case ETN:
                fidCondMrktDivCode = "J";
                fidInputIscd = "Q" + asset.getSymbol();
                break;
            default:
                throw new RuntimeException("invalid asset type - " + asset.getType());
        }
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
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

        Double price = output2.getNumber("stck_prpr").doubleValue();
        Double bidPrice = output1.getNumber("bidp1").doubleValue();
        Double askPrice = output1.getNumber("askp1").doubleValue();

        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) {
        List<Ohlcv> minuteOhlcvs = new ArrayList<>();
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String fidEtcClsCode = "";
        String fidCondMrktDivCode = "J";
        String fidInputIscd;
        switch(asset.getType()) {
            case STOCK:
            case ETF:
                fidInputIscd = asset.getSymbol();
                break;
            case ETN:
                fidInputIscd = "Q" + asset.getSymbol();
                break;
            default:
                throw new RuntimeException("invalid asset type - " + asset.getType());
        }
        LocalTime nowTime = LocalTime.now();
        LocalTime closeTime = LocalTime.of(15,30);
        LocalTime fidInputHour1Time = (nowTime.isAfter(closeTime) ? closeTime : nowTime);

        for(int i = 0; i < 3; i ++) {
            String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice";
            HttpHeaders headers = createHeaders();
            headers.add("tr_id", "FHKST03010200");
            headers.add("custtype", "P");
            String fidInputHour1 = fidInputHour1Time.format(DateTimeFormatter.ofPattern("HHmmss"));
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("FID_ETC_CLS_CODE", fidEtcClsCode)
                    .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                    .queryParam("FID_INPUT_ISCD", fidInputIscd)
                    .queryParam("FID_INPUT_HOUR_1", fidInputHour1)
                    .queryParam("FID_PW_DATA_INCU_YN","N")
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

            List<Ohlcv> minuteOhlcvsPage = output2.stream()
                    .map(row -> {
                        LocalDateTime dateTime = LocalDateTime.parse(
                                row.getString("stck_bsop_date") + row.getString("stck_cntg_hour"),
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        );
                        Double openPrice = row.getNumber("stck_oprc").doubleValue();
                        Double highPrice = row.getNumber("stck_hgpr").doubleValue();
                        Double lowPrice = row.getNumber("stck_lwpr").doubleValue();
                        Double closePrice = row.getNumber("stck_prpr").doubleValue();
                        Double volume = row.getNumber("cntg_vol").doubleValue();
                        return Ohlcv.builder()
                                .dateTime(dateTime)
                                .openPrice(openPrice)
                                .highPrice(highPrice)
                                .lowPrice(lowPrice)
                                .closePrice(closePrice)
                                .volume(volume)
                                .build();
                    })
                    .collect(Collectors.toList());
            minuteOhlcvs.addAll(minuteOhlcvsPage);

            // check empty
            if(minuteOhlcvsPage.size() < 1) {
                break;
            }

            // next page
            fidInputHour1Time = minuteOhlcvsPage.get(minuteOhlcvsPage.size()-1)
                    .getDateTime()
                    .toLocalTime()
                    .minusMinutes(1);
        }

        // return
        return minuteOhlcvs;
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String fidCondMrktDivCode = "J";
        String fidInputIscd;
        switch(asset.getType()) {
            case STOCK:
            case ETF:
                fidInputIscd = asset.getSymbol();
                break;
            case ETN:
                fidCondMrktDivCode = "J";
                fidInputIscd = "Q" + asset.getSymbol();
                break;
            default:
                throw new RuntimeException("invalid asset type - " + asset.getType());
        }

        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-price";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010400");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_PERIOD_DIV_CODE", "D") // 일봉
                .queryParam("FID_ORG_ADJ_PRC", "0")     // 수정주가
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

        List<ValueMap> output = objectMapper.convertValue(rootNode.path("output"), new TypeReference<>(){});

        return output.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(row.getString("stck_bsop_date")+"000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Double openPrice = row.getNumber("stck_oprc").doubleValue();
                    Double highPrice = row.getNumber("stck_hgpr").doubleValue();
                    Double lowPrice = row.getNumber("stck_lwpr").doubleValue();
                    Double closePrice = row.getNumber("stck_clpr").doubleValue();
                    Double volume = row.getNumber("acml_vol").doubleValue();
                    return Ohlcv.builder()
                            .dateTime(dateTime)
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
    public Balance getBalance() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC8424R" : "VTTC8434R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("AFHR_FLPR_YN", "N")
                .queryParam("OFL_YN", "")
                .queryParam("INQR_DVSN", "02")
                .queryParam("UNPR_DVSN", "01")
                .queryParam("FUND_STTL_ICLD_YN", "N")
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                .queryParam("PRCS_DVSN", "00")
                .queryParam("CTX_AREA_FK100", "")
                .queryParam("CTX_AREA_NK100", "")
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
        JsonNode output1Node = rootNode.path("output1");
        List<ValueMap> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});

        JsonNode output2Node = rootNode.path("output2");
        List<ValueMap> output2 = objectMapper.convertValue(output2Node, new TypeReference<List<ValueMap>>(){});

        Balance balance = Balance.builder()
                .accountNo(accountNo)
                .totalAmount(output2.get(0).getNumber("tot_evlu_amt").doubleValue())
                .cashAmount(output2.get(0).getNumber("dnca_tot_amt").doubleValue())
                .purchaseAmount(output2.get(0).getNumber("pchs_amt_smtl_amt").doubleValue())
                .valuationAmount(output2.get(0).getNumber("evlu_amt_smtl_amt").doubleValue())
                .gainLossAmount(output2.get(0).getNumber("evlu_pfls_amt").doubleValue())
                .realizedGainLossAmount(output2.get(0).getNumber("rlzt_pfls").doubleValue())
                .build();

        List<BalanceAsset> balanceAssets = output1.stream()
                .map(row -> BalanceAsset.builder()
                        .accountNo(accountNo)
                        .symbol(row.getString("pdno"))
                        .name(row.getString("prdt_name"))
                        .quantity(row.getNumber("hldg_qty").intValue())
                        .purchaseAmount(row.getNumber("pchs_amt").doubleValue())
                        .valuationAmount(row.getNumber("evlu_amt").doubleValue())
                        .gainLossAmount(row.getNumber("evlu_pfls_amt").doubleValue())
                        .build())
                .collect(Collectors.toList());
        balance.setBalanceAssets(balanceAssets);

        return balance;
    }

    @Override
    public void buyAsset(TradeAsset tradeAsset, int quantity) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC0802U" : "VTTC0802U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", accountNo.split("-")[0]);
            put("ACNT_PRDT_CD", accountNo.split("-")[1]);
            put("PDNO", tradeAsset.getSymbol());
            put("ORD_DVSN", "01");
            put("ORD_QTY", String.valueOf(quantity));
            put("ORD_UNPR", "0");
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        sleep();
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg1 = responseMap.getString("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }
    }

    @Override
    public void sellAsset(BalanceAsset balanceAsset, int quantity) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC0801U" : "VTTC0801U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", accountNo.split("-")[0]);
            put("ACNT_PRDT_CD", accountNo.split("-")[1]);
            put("PDNO", balanceAsset.getSymbol());
            put("ORD_DVSN", "01");
            put("ORD_QTY", String.valueOf(quantity));
            put("ORD_UNPR", "0");
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        sleep();
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg1 = responseMap.getString("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }
    }

}
