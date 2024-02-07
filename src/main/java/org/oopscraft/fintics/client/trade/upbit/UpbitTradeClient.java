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
import org.oopscraft.fintics.client.trade.TradeClientDefinition;
import org.oopscraft.fintics.model.*;
import org.springframework.core.ParameterizedTypeReference;
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

    public UpbitTradeClient(TradeClientDefinition definition, Properties config) {
        super(definition, config);
        this.accessKey = config.getProperty("accessKey");
        this.secretKey = config.getProperty("secretKey");
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        return true;
    }

    @Override
    public List<Asset> getAssets() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(API_URL + "/v1/market/all")
                .build();
        ResponseEntity<List<ValueMap>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<ValueMap>>() {});
        return responseEntity.getBody().stream()
                .map(map -> {
                    return Asset.builder()
                            .assetId(toAssetId(map.getString("market")))
                            .assetName(map.getString("english_name"))
                            .build();
                })
                .collect(Collectors.toList());
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
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        return getOhlcvs(asset, Ohlcv.Type.MINUTE);
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        return getOhlcvs(asset, Ohlcv.Type.DAILY);
    }

    private List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type ohlcvType) throws InterruptedException {
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
                            .type(ohlcvType)
                            .dateTime(dateTime)
                            .openPrice(row.getNumber("opening_price").setScale(2, RoundingMode.HALF_UP))
                            .highPrice(row.getNumber("high_price").setScale(2, RoundingMode.HALF_UP))
                            .lowPrice(row.getNumber("low_price").setScale(2, RoundingMode.HALF_UP))
                            .closePrice(row.getNumber("trade_price").setScale(2, RoundingMode.HALF_UP))
                            .volume(row.getNumber("candle_acc_trade_volume").setScale(2, RoundingMode.HALF_UP))
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
                        .assetId(toAssetId(symbol))
                        .assetName(symbol)
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
    public Order submitOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        // define parameters
        String market = order.getSymbol();
        String side;
        String ordType;
        BigDecimal price;
        BigDecimal volume;
        switch(order.getType()) {
            case BUY -> {
                side = "bid";
                switch(order.getKind()) {
                    case LIMIT -> {
                        ordType = "limit";
                        price = order.getPrice();
                        volume = order.getQuantity();
                    }
                    case MARKET -> {
                        ordType = "price";
                        price = order.getPrice().multiply(order.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP);
                        volume = null;
                    }
                    default -> throw new RuntimeException("Invalid order type");
                }
            }
            case SELL -> {
                side = "ask";
                switch(order.getKind()) {
                    case LIMIT -> {
                        ordType = "limit";
                        price = order.getPrice();
                        volume = order.getQuantity();
                    }
                    case MARKET -> {
                        ordType = "market";
                        price = null;
                        volume = order.getQuantity();
                    }
                    default -> throw new RuntimeException("Invalid order type");
                }
            }
            default -> throw new RuntimeException("Invalid order kind");
        }

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
        ValueMap responseMap = responseEntity.getBody();
        log.info("{}", responseMap);
        if(responseMap != null) {
            order.setBrokerOrderId(responseMap.getString("uuid"));
        }

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        String url = API_URL + "/v1/orders/";
        String queryString = "state=wait&page=1&limit=100";
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
                    Order.Type orderKind;
                    switch(row.getString("side")) {
                        case "bid" -> orderKind = Order.Type.BUY;
                        case "ask" -> orderKind = Order.Type.SELL;
                        default -> throw new RuntimeException("invalid side");
                    }
                    Order.Kind orderType;
                    switch(row.getString("ord_type")) {
                        case "limit" -> orderType = Order.Kind.LIMIT;
                        case "market","price" -> orderType = Order.Kind.MARKET;
                        default -> orderType = null;
                    }
                    String symbol = row.getString("market");
                    BigDecimal quantity = row.getNumber("remaining_volume");
                    BigDecimal price = row.getNumber("price");
                    String clientOrderId = row.getString("uuid");

                    // order
                    return Order.builder()
                            .type(orderKind)
                            .assetId(toAssetId(symbol))
                            .kind(orderType)
                            .quantity(quantity)
                            .price(price)
                            .brokerOrderId(clientOrderId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        // cancel
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = API_URL + "/v1/order";
        String queryString = "uuid=" + order.getBrokerOrderId();
        RequestEntity<Void> requestEntity = RequestEntity
                .delete(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        restTemplate.exchange(requestEntity, Void.class);

        // submit order
        return submitOrder(order);
    }

}
