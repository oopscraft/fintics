package org.oopscraft.fintics.client.trade.upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UpbitTradeClient extends TradeClient {

    private static final String API_URL = "https://api.upbit.com";

    private static final String QUERY_HASH_ALGORITHM = "SHA-512";

    private final String accessKey;

    private final String secretKey;

    private final ObjectMapper objectMapper;

    public UpbitTradeClient(Properties properties) {
        super(properties);
        this.accessKey = properties.getProperty("accessKey");
        this.secretKey = properties.getProperty("secretKey");
        this.objectMapper = new ObjectMapper();
    }

    private synchronized static void sleep() throws InterruptedException {
        Thread.sleep(300);
    }

    HttpHeaders createHeaders(String queryString) {
        // check null
        if(queryString == null) {
            queryString = "";
        }

        // query hash
        String queryHash;
        try {
            MessageDigest md = MessageDigest.getInstance(QUERY_HASH_ALGORITHM);
            md.update(queryString.getBytes(StandardCharsets.UTF_8));
            queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }

        // jwt token
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        // http header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Bearer %s", jwtToken));
        return httpHeaders;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = API_URL + "/v1/orderbook";
        String queryString = "markets=" + asset.getSymbol();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ValueMap> orderBookUnits = objectMapper.convertValue(rootNode.get(0).path("orderbook_units"), new TypeReference<>() {});
        ValueMap orderBookUnit = orderBookUnits.get(0);
        return OrderBook.builder()
                .price(orderBookUnit.getNumber("bid_price"))
                .bidPrice(orderBookUnit.getNumber("bid_price"))
                .askPrice(orderBookUnit.getNumber("ask_price"))
                .build();
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        return getOhlcvs(asset, OhlcvType.MINUTE);
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        return getOhlcvs(asset, OhlcvType.DAILY);
    }

    private List<Ohlcv> getOhlcvs(Asset asset, OhlcvType ohlcvType) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = API_URL + "/v1/candles/";
        switch(ohlcvType) {
            case MINUTE -> url += "minutes/1";
            case DAILY -> url += "days";
            default -> throw new RuntimeException("invalid OhlcvType");
        }
        String queryString = "market=" + asset.getSymbol() + "&count=200";
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();

        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        List<ValueMap> rows;
        try {
            rows = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(
                            row.getString("candle_date_time_kst"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    return Ohlcv.builder()
                            .ohlcvType(ohlcvType)
                            .dateTime(dateTime)
                            .openPrice(row.getNumber("opening_price"))
                            .highPrice(row.getNumber("high_price"))
                            .lowPrice(row.getNumber("low_price"))
                            .closePrice(row.getNumber("trade_price"))
                            .volume(row.getNumber("candle_acc_trade_volume"))
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Override
    public Balance getBalance() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        RequestEntity<Void> requestEntity = RequestEntity
                .get(API_URL + "/v1/accounts")
                .headers(createHeaders(null))
                .build();

        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        List<ValueMap> rows;
        try {
            rows = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal cacheAmount = BigDecimal.ZERO;
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal valuationAmount = BigDecimal.ZERO;
        BigDecimal profitAmount = BigDecimal.ZERO;
        BigDecimal realizedProfitAmount = BigDecimal.ZERO;
        List<BalanceAsset> balanceAssets = new ArrayList<>();
        for(ValueMap row : rows) {
            String currency = row.getString("currency");
            String unitCurrency = row.getString("unit_currency");
            String symbol = String.format("%s-%s", unitCurrency, currency);
            BigDecimal balance = row.getNumber("balance");
            BigDecimal averageBuyPrice = row.getNumber("avg_buy_price");
            totalAmount = totalAmount.add(balance);
            if(currency.equals(unitCurrency)) {
                cacheAmount = cacheAmount.add(balance);
            }else{
                BigDecimal assetPurchaseAmount = averageBuyPrice.multiply(balance)
                        .setScale(0, RoundingMode.HALF_UP);
                purchaseAmount = purchaseAmount.add(assetPurchaseAmount);
                balanceAssets.add(BalanceAsset.builder()
                        .symbol(symbol)
                        .name(symbol)
                        .quantity(balance)
                        .orderableQuantity(balance)
                        .purchaseAmount(purchaseAmount)
                        .build());
            }
        }
        return Balance.builder()
                .totalAmount(totalAmount)
                .cashAmount(cacheAmount)
                .purchaseAmount(purchaseAmount)
                .valuationAmount(valuationAmount)
                .profitAmount(profitAmount)
                .realizedProfitAmount(realizedProfitAmount)
                .balanceAssets(balanceAssets)
                .build();
    }

    @Override
    public void buyAsset(Asset asset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        switch(orderType) {
            case LIMIT -> order(asset.getSymbol(), "bid", "limit", price, quantity);
            case MARKET -> {
                BigDecimal orderPrice = price.multiply(quantity)
                        .setScale(2, RoundingMode.HALF_UP);
                order(asset.getSymbol(), "bid", "price", orderPrice, null);
            }
            default -> throw new RuntimeException("Invalid order type");
        }
    }

    @Override
    public void sellAsset(Asset asset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        switch(orderType) {
            case LIMIT -> order(asset.getSymbol(), "ask", "limit", price, quantity);
            case MARKET -> order(asset.getSymbol(), "ask", "market", null, quantity);
            default -> throw new RuntimeException("Invalid order type");
        }
    }

    private void order(String market, String side, String ordType, BigDecimal price, BigDecimal volume) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        // url payload
        String url = API_URL + "/v1/orders";
        HashMap<String,String> payloadMap = new HashMap<>();
        payloadMap.put("market", market);
        payloadMap.put("side", side);
        if(price != null) {
            payloadMap.put("price", String.valueOf(price.doubleValue()));
        }
        if(volume != null) {
            payloadMap.put("volume", String.valueOf(volume.doubleValue()));
        }
        payloadMap.put("ord_type", ordType);
        payloadMap.put("identifier", IdGenerator.uuid());

        // query string
        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : payloadMap.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }
        String queryString = String.join("&", queryElements.toArray(new String[0]));

        // payload
        String payload;
        try {
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // request
        RequestEntity<String> requestEntity = RequestEntity
                .post(url)
                .headers(createHeaders(queryString))
                .header("Content-Type", "application/json")
                .body(payload);

        sleep();
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        log.info("{}",responseEntity.getBody());
    }

}
