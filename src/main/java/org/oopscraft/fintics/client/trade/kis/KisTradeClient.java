package org.oopscraft.fintics.client.trade.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.trade.TradeClient;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KisTradeClient extends TradeClient {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    public KisTradeClient(Properties properties) {
        super(properties);
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
        ZonedDateTime systemZonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime koreaZonedDateTime = systemZonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

        // weekend
        DayOfWeek dayOfWeek = koreaZonedDateTime.getDayOfWeek();
        if(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // check holiday
        if(isHoliday(koreaZonedDateTime.toLocalDateTime())) {
            return false;
        }

        // default
        return true;
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

        List<ValueMap> output = objectMapper.convertValue(rootNode.path("output"), new TypeReference<>(){});
        ValueMap matchedRow = output.stream()
                .filter(row -> {
                    LocalDate localDate = LocalDate.parse(row.getString("bass_dt"), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    return localDate.isEqual(dateTime.toLocalDate());
                })
                .findFirst()
                .orElse(null);

        // define holiday
        boolean holiday = false;
        if(matchedRow != null) {
            String openYn = matchedRow.getString("opnd_yn");
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
        String fidInputIscd = asset.getAssetId();
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

            List<ValueMap> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

            List<Ohlcv> minuteOhlcvsPage = output2.stream()
                    .map(row -> {
                        LocalDateTime ohlcvDateTime = LocalDateTime.parse(
                                row.getString("stck_bsop_date") + row.getString("stck_cntg_hour"),
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        );
                        BigDecimal openPrice = row.getNumber("stck_oprc");
                        BigDecimal highPrice = row.getNumber("stck_hgpr");
                        BigDecimal lowPrice = row.getNumber("stck_lwpr");
                        BigDecimal closePrice = row.getNumber("stck_prpr");
                        BigDecimal volume = row.getNumber("cntg_vol");
                        return Ohlcv.builder()
                                .ohlcvType(OhlcvType.MINUTE)
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
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getAssetId();

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
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(row.getString("stck_bsop_date")+"000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    BigDecimal openPrice = row.getNumber("stck_oprc");
                    BigDecimal highPrice = row.getNumber("stck_hgpr");
                    BigDecimal lowPrice = row.getNumber("stck_lwpr");
                    BigDecimal closePrice = row.getNumber("stck_clpr");
                    BigDecimal volume = row.getNumber("acml_vol");
                    return Ohlcv.builder()
                            .ohlcvType(OhlcvType.DAILY)
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
    public OrderBook getOrderBook(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010200");
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getAssetId();
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

        BigDecimal price = output2.getNumber("stck_prpr");
        BigDecimal bidPrice = output1.getNumber("bidp1");
        BigDecimal askPrice = output1.getNumber("askp1");

        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
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
        List<ValueMap> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});

        JsonNode output2Node = rootNode.path("output2");
        List<ValueMap> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});

        Balance balance = Balance.builder()
                .accountNo(accountNo)
                .totalAmount(output2.get(0).getNumber("tot_evlu_amt"))
                .cashAmount(output2.get(0).getNumber("prvs_rcdl_excc_amt"))
                .purchaseAmount(output2.get(0).getNumber("pchs_amt_smtl_amt"))
                .valuationAmount(output2.get(0).getNumber("evlu_amt_smtl_amt"))
                .build();

        List<BalanceAsset> balanceAssets = output1.stream()
                .map(row -> BalanceAsset.builder()
                        .accountNo(accountNo)
                        .assetId(row.getString("pdno"))
                        .assetName(row.getString("prdt_name"))
                        .quantity(row.getNumber("hldg_qty"))
                        .orderableQuantity(row.getNumber("ord_psbl_qty"))
                        .purchaseAmount(row.getNumber("pchs_amt"))
                        .valuationAmount(row.getNumber("evlu_amt"))
                        .profitAmount(row.getNumber("evlu_pfls_amt"))
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
        List<ValueMap> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});
        return output2.get(0).getNumber("rlzt_pfls");
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();

        // order kind
        String trId = null;
        switch(order.getOrderType()) {
            case BUY -> trId = production ? "TTTC0802U" : "VTTC0802U";
            case SELL -> trId = production ? "TTTC0801U" : "VTTC0801U";
            default -> throw new RuntimeException("invalid order kind");
        }
        headers.add("tr_id", trId);

        // order type
        String ordDvsn = null;
        String ordUnpr = null;
        switch(order.getOrderKind()) {
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

        // quantity
        int quantity = Math.max(order.getQuantity().intValue(),1);

        // request
        ValueMap payloadMap = new ValueMap();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("PDNO", order.getAssetId());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("ORD_QTY", String.valueOf(quantity));
        payloadMap.put("ORD_UNPR", ordUnpr);
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getString("rt_cd");
        String msg1 = responseMap.getString("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        // 모의 투자는 지원 하지 않음
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
        List<ValueMap> output = objectMapper.convertValue(outputNode, new TypeReference<>(){});

        // return
        return output.stream()
                .map(row -> {
                    OrderType orderType;
                    switch (row.getString("sll_buy_dvsn_cd")) {
                        case "01" -> orderType = OrderType.SELL;
                        case "02" -> orderType = OrderType.BUY;
                        default -> throw new RuntimeException("invalid sll_buy_dvsn_cd");
                    }
                    OrderKind orderKind;
                    switch (row.getString("ord_dvsn_cd")) {
                        case "00" -> orderKind = OrderKind.LIMIT;
                        case "01" -> orderKind = OrderKind.MARKET;
                        default -> orderKind = null;
                    }

                    String symbol = row.getString("pdno");
                    BigDecimal quantity = row.getNumber("psbl_qty");
                    BigDecimal price = row.getNumber("ord_unpr");
                    String clientOrderId = row.getString("odno");
                    return Order.builder()
                            .orderType(orderType)
                            .assetId(symbol)
                            .orderKind(orderKind)
                            .quantity(quantity)
                            .price(price)
                            .clientOrderId(clientOrderId)
                            .build();

                })
                .collect(Collectors.toList());
    }

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();

        // trId
        String trId = (production ? "TTTC0803U" : "VTTC0803U");
        headers.add("tr_id", trId);

        // order type
        String ordDvsn = null;
        switch(order.getOrderKind()) {
            case LIMIT -> {
                ordDvsn = "00";
            }
            case MARKET -> {
                ordDvsn = "01";
            }
            default -> throw new RuntimeException("invalid order type");
        }

        // request
        ValueMap payloadMap = new ValueMap();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("KRX_FWDG_ORD_ORGNO", "");
        payloadMap.put("ORGN_ODNO", order.getClientOrderId());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("RVSE_CNCL_DVSN_CD", "01");
        payloadMap.put("ORD_QTY", "0");
        payloadMap.put("ORD_UNPR", String.valueOf(order.getPrice().longValue()));
        payloadMap.put("QTY_ALL_ORD_YN", "Y");
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getString("rt_cd");
        String msg1 = responseMap.getString("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

}
