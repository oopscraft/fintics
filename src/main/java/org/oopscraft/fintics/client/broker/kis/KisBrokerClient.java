package org.oopscraft.fintics.client.broker.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.KrBrokerClient;
import org.oopscraft.fintics.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KisBrokerClient extends KrBrokerClient {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    public KisBrokerClient(BrokerClientDefinition definition, Properties properties) {
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
        // check kr super (weekend)
        if (!super.isOpened(dateTime)) {
            return false;
        }

        // check holiday
        ZonedDateTime systemZonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime koreaZonedDateTime = systemZonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return !isHoliday(koreaZonedDateTime.toLocalDateTime());
    }

    boolean isHoliday(LocalDateTime dateTime) throws InterruptedException {
        // 모의 투자는 휴장일 조회 API 제공 하지 않음
        if(!production) {
            return false;
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/chk-holiday";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "CTCA0903R");

        String baseDt = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("BASS_DT", baseDt)
                .queryParam("CTX_AREA_NK","")
                .queryParam("CTX_AREA_FK","")
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

        List<Map<String, String>> output = objectMapper.convertValue(rootNode.path("output"), new TypeReference<>(){});
        Map<String, String> matchedRow = output.stream()
                .filter(row -> {
                    LocalDate localDate = LocalDate.parse(row.get("bass_dt"), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    return localDate.isEqual(dateTime.toLocalDate());
                })
                .findFirst()
                .orElse(null);

        // define holiday
        boolean holiday = false;
        if(matchedRow != null) {
            String openYn = matchedRow.get("opnd_yn");
            if(openYn.equals("N")) {
                holiday = true;
            }
        }
        return holiday;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        List<Ohlcv> minuteOhlcvs = new ArrayList<>();
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String fidEtcClsCode = "";
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
        LocalTime time = dateTime.toLocalTime();
        LocalTime closeTime = LocalTime.of(15,30);
        LocalTime fidInputHour1Time = (time.isAfter(closeTime) ? closeTime : time);

        for(int i = 0; i < 20; i ++) {
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

            List<Map<String, String>> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

            List<Ohlcv> minuteOhlcvsPage = output2.stream()
                    .map(row -> {
                        LocalDateTime ohlcvDateTime = LocalDateTime.parse(
                                row.get("stck_bsop_date") + row.get("stck_cntg_hour"),
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        );
                        BigDecimal openPrice = new BigDecimal(row.get("stck_oprc"));
                        BigDecimal highPrice = new BigDecimal(row.get("stck_hgpr"));
                        BigDecimal lowPrice = new BigDecimal(row.get("stck_lwpr"));
                        BigDecimal closePrice = new BigDecimal(row.get("stck_prpr"));
                        BigDecimal volume = new BigDecimal(row.get("cntg_vol"));
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
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST03010100");
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
        String fidInputDate1 = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now().minusMonths(1));
        String fidInputDate2 = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String fidPeriodDivCode = "D";  // 일봉
        String fidOrgAdjPrc = "0";      // 수정주가
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_DATE_1", fidInputDate1)
                .queryParam("FID_INPUT_DATE_2", fidInputDate2)
                .queryParam("FID_PERIOD_DIV_CODE", fidPeriodDivCode)
                .queryParam("FID_ORG_ADJ_PRC", fidOrgAdjPrc)
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

        List<Map<String, String>> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

        return output2.stream()
                .map(row -> {
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(row.get("stck_bsop_date")+"000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    BigDecimal openPrice = new BigDecimal(row.get("stck_oprc"));
                    BigDecimal highPrice = new BigDecimal(row.get("stck_hgpr"));
                    BigDecimal lowPrice = new BigDecimal(row.get("stck_lwpr"));
                    BigDecimal closePrice = new BigDecimal(row.get("stck_clpr"));
                    BigDecimal volume = new BigDecimal(row.get("acml_vol"));
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
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010200");
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
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

        Map<String, String> output1 = objectMapper.convertValue(rootNode.path("output1"), new TypeReference<>() {});
        Map<String, String> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>() {});

        BigDecimal price = new BigDecimal(output2.get("stck_prpr"));
        BigDecimal bidPrice = new BigDecimal(output1.get("bidp1"));
        BigDecimal askPrice = new BigDecimal(output1.get("askp1"));

        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public BigDecimal getMinimumOrderQuantity() throws InterruptedException {
        return BigDecimal.ONE;
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC8434R" : "VTTC8434R";
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

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        JsonNode output1Node = rootNode.path("output1");
        List<Map<String, String>> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});

        JsonNode output2Node = rootNode.path("output2");
        List<Map<String, String>> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});

        Balance balance = Balance.builder()
                .accountNo(accountNo)
                .totalAmount(new BigDecimal(output2.get(0).get("tot_evlu_amt")))
                .cashAmount(new BigDecimal(output2.get(0).get("prvs_rcdl_excc_amt")))
                .purchaseAmount(new BigDecimal(output2.get(0).get("pchs_amt_smtl_amt")))
                .valuationAmount(new BigDecimal(output2.get(0).get("evlu_amt_smtl_amt")))
                .build();

        List<BalanceAsset> balanceAssets = output1.stream()
                .map(row -> BalanceAsset.builder()
                        .accountNo(accountNo)
                        .assetId(toAssetId(row.get("pdno")))
                        .assetName(row.get("prdt_name"))
                        .quantity(new BigDecimal(row.get("hldg_qty")))
                        .orderableQuantity(new BigDecimal(row.get("ord_psbl_qty")))
                        .purchaseAmount(new BigDecimal(row.get("pchs_amt")))
                        .valuationAmount(new BigDecimal(row.get("evlu_amt")))
                        .profitAmount(new BigDecimal(row.get("evlu_pfls_amt")))
                        .build())
                .filter(balanceAsset -> balanceAsset.getQuantity().intValue() > 0)
                .collect(Collectors.toList());
        balance.setBalanceAssets(balanceAssets);

        BigDecimal profitAmount = balanceAssets.stream()
                .map(BalanceAsset::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance.setProfitAmount(profitAmount);

        if(production) {
            BigDecimal realizedProfitAmount = getBalanceRealizedProfitAmount();
            balance.setRealizedProfitAmount(realizedProfitAmount);
        }

        return balance;
    }

    private BigDecimal getBalanceRealizedProfitAmount() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance-rlz-pl";
        HttpHeaders headers = createHeaders();
        String trId = "TTTC8494R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("AFHR_FLPR_YN", "N")
                .queryParam("OFL_YN", "")
                .queryParam("INQR_DVSN", "00")
                .queryParam("UNPR_DVSN", "01")
                .queryParam("FUND_STTL_ICLD_YN", "N")
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                .queryParam("PRCS_DVSN", "00")
                .queryParam("COST_ICLD_YN", "Y")
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

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        JsonNode output2Node = rootNode.path("output2");
        List<Map<String, String>> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});
        return new BigDecimal(output2.get(0).get("rlzt_pfls"));
    }

    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();

        // order type
        String trId = null;
        switch(order.getType()) {
            case BUY -> trId = production ? "TTTC0802U" : "VTTC0802U";
            case SELL -> trId = production ? "TTTC0801U" : "VTTC0801U";
            default -> throw new RuntimeException("invalid order kind");
        }
        headers.add("tr_id", trId);

        // order kind
        String ordDvsn = null;
        String ordUnpr = null;
        switch(order.getKind()) {
            case LIMIT -> {
                ordDvsn = "00";
                ordUnpr = String.valueOf(order.getPrice().longValue());
            }
            case MARKET -> {
                ordDvsn = "01";
                ordUnpr = "0";
            }
            default -> throw new RuntimeException("invalid order type");
        }

        // quantity with check
        int quantity = order.getQuantity().intValue();

        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("ORD_QTY", String.valueOf(quantity));
        payloadMap.put("ORD_UNPR", ordUnpr);
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>(){});
        Map<String, Object> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getOrDefault("rt_cd", "").toString();
        String msg1 = responseMap.getOrDefault("msg1", "").toString();
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        // supported in only production
        if(!production) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-psbl-rvsecncl";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "TTTC8036R");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("CTX_AREA_FK100", "")
                .queryParam("CTX_AREA_NK100","")
                .queryParam("INQR_DVSN_1", "1")
                .queryParam("INQR_DVSN_2", "0")
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

        JsonNode outputNode = rootNode.path("output");
        List<Map<String, String>> output = objectMapper.convertValue(outputNode, new TypeReference<>(){});

        // return
        return output.stream()
                .map(row -> {
                    Order.Type orderType;
                    switch (row.get("sll_buy_dvsn_cd")) {
                        case "01" -> orderType = Order.Type.SELL;
                        case "02" -> orderType = Order.Type.BUY;
                        default -> throw new RuntimeException("invalid sll_buy_dvsn_cd");
                    }
                    Order.Kind orderKind;
                    switch (row.get("ord_dvsn_cd")) {
                        case "00" -> orderKind = Order.Kind.LIMIT;
                        case "01" -> orderKind = Order.Kind.MARKET;
                        default -> orderKind = null;
                    }

                    String symbol = row.get("pdno");
                    BigDecimal quantity = new BigDecimal(row.get("psbl_qty"));
                    BigDecimal price = new BigDecimal(row.get("ord_unpr"));
                    String clientOrderId = row.get("odno");
                    return Order.builder()
                            .type(orderType)
                            .assetId(toAssetId(symbol))
                            .kind(orderKind)
                            .quantity(quantity)
                            .price(price)
                            .brokerOrderId(clientOrderId)
                            .build();

                })
                .collect(Collectors.toList());
    }

    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-rvsecncl";
        HttpHeaders headers = createHeaders();

        // trId
        String trId = (production ? "TTTC0803U" : "VTTC0803U");
        headers.add("tr_id", trId);

        // order type
        String ordDvsn = null;
        switch(order.getKind()) {
            case LIMIT -> {
                ordDvsn = "00";
            }
            case MARKET -> {
                ordDvsn = "01";
            }
            default -> throw new RuntimeException("invalid order type");
        }

        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("KRX_FWDG_ORD_ORGNO", "");
        payloadMap.put("ORGN_ODNO", order.getBrokerOrderId());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("RVSE_CNCL_DVSN_CD", "01");
        payloadMap.put("ORD_QTY", "0");
        payloadMap.put("ORD_UNPR", String.valueOf(order.getPrice().longValue()));
        payloadMap.put("QTY_ALL_ORD_YN", "Y");
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>(){});
        Map<String, Object> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getOrDefault("rt_cd", "").toString();
        String msg1 = responseMap.getOrDefault("msg1", "").toString();
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

}
