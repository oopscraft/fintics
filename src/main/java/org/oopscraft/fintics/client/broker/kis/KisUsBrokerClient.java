package org.oopscraft.fintics.client.broker.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.UsBrokerClient;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTS3012R" : "VTTS3012R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")
                .queryParam("TR_CRCY_CD", "USD")
                .queryParam("CTX_AREA_FK200", "")
                .queryParam("CTX_AREA_NK200", "")
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
        ValueMap output2 = objectMapper.convertValue(output2Node, ValueMap.class);

        // balance
        Balance balance = Balance.builder()
                .accountNo(accountNo)
                .purchaseAmount(output2.getNumber("frcr_pchs_amt1"))
                .valuationAmount(output2.getNumber("tot_evlu_pfls_amt"))
                .realizedProfitAmount(output2.getNumber("ovrs_rlzt_pfls_amt"))
                .profitAmount(output2.getNumber("ovrs_tot_pfls"))
                .build();

        // cash amount, total amount
        BigDecimal cashAmount = getBalanceCashAmount();
        BigDecimal totalAmount = balance.getValuationAmount().add(cashAmount);
        balance.setTotalAmount(totalAmount);
        balance.setCashAmount(cashAmount);

        // balance asset
        List<BalanceAsset> balanceAssets = output1.stream()
                .map(row -> BalanceAsset.builder()
                        .accountNo(accountNo)
                        .assetId(toAssetId(row.getString("ovrs_pdno")))
                        .assetName(row.getString("ovrs_item_name"))
                        .quantity(row.getNumber("ovrs_cblc_qty"))
                        .orderableQuantity(row.getNumber("ord_psbl_qty"))
                        .purchasePrice(row.getNumber("pchs_avg_pric"))
                        .purchaseAmount(row.getNumber("frcr_pchs_amt1"))
                        .valuationAmount(row.getNumber("ovrs_stck_evlu_amt"))
                        .profitAmount(row.getNumber("frcr_evlu_pfls_amt"))
                        .build())
                .filter(balanceAsset -> balanceAsset.getQuantity().intValue() > 0)
                .collect(Collectors.toList());
        balance.setBalanceAssets(balanceAssets);

        // return
        return balance;
    }

    private BigDecimal getBalanceCashAmount() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-present-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "CTRP6504R" : "VTRP6504R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("WCRC_FRCR_DVSN_CD", "02")
                .queryParam("NATN_CD", "840")
                .queryParam("TR_MKET_CD", "00")
                .queryParam("INQR_DVSN_CD", "00")
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
        return output2.get(0).getNumber("frcr_dncl_amt_2");
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();

        // order type
        String trId = null;
        switch(order.getType()) {
            case BUY -> trId = production ? "TTTT1002U" : "VTTT1002U";
            case SELL -> trId = production ? "TTTT1006U" : "VTTT1001U";
            default -> throw new RuntimeException("invalid order kind");
        }
        headers.add("tr_id", trId);

        // quantity
        int quantity = Math.max(order.getQuantity().intValue(),1);

        // price
        double price = order.getPrice()
                .setScale(2, RoundingMode.FLOOR)
                .doubleValue();

        // request
        ValueMap payloadMap = new ValueMap();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", "NASD");
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORD_QTY", String.valueOf(quantity));
        payloadMap.put("OVRS_ORD_UNPR", String.valueOf(price));
        payloadMap.put("ORD_SVR_DVSN_CD", "0");
        payloadMap.put("ORD_DVSN", "00");
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
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        HttpHeaders headers = createHeaders();
        headers.add("", production ? "TTTS3018R" : "VTTS3018R");
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-nccs";
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")
                .queryParam("CTX_AREA_FK200","")
                .queryParam("CTX_AREA_NK200", "")
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
                    Order.Type orderType;
                    switch (row.getString("sll_buy_dvsn_cd")) {
                        case "01" -> orderType = Order.Type.SELL;
                        case "02" -> orderType = Order.Type.BUY;
                        default -> throw new RuntimeException("invalid sll_buy_dvsn_cd");
                    }
                    Order.Kind orderKind = Order.Kind.LIMIT;
                    String symbol = row.getString("pdno");
                    BigDecimal quantity = row.getNumber("ft_ord_qty");
                    BigDecimal price = row.getNumber("ft_ord_unpr3");
                    String clientOrderId = row.getString("odno");
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
    public Order amendOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/order-rvsecncl";
        String trId = (production ? "TTTT1004U" : "VTTT1004U");
        BigDecimal quantity = order.getQuantity();
        BigDecimal price = order.getPrice().setScale(2, RoundingMode.FLOOR);

        // request
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", "NASD");
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORGN_ODNO", order.getBrokerOrderId());
        payloadMap.put("RVSE_CNCL_DVSN_CD", "01");
        payloadMap.put("ORD_QTY", quantity.toString());
        payloadMap.put("OVRS_ORD_UNPR", price.toString());
        payloadMap.put("ORD_SVR_DVSN_CD", "0");
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
