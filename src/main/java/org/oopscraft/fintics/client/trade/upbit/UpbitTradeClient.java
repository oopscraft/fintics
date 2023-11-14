package org.oopscraft.fintics.client.trade.upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UpbitTradeClient extends TradeClient {

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

    String createJwtToken(String queryHash) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", Optional.ofNullable(queryHash).orElse(""))
                .withClaim("query_hash_alg", QUERY_HASH_ALGORITHM)
                .sign(algorithm);
    }

    String createAuthorizationHeader(String queryHash) {
        return String.format("Bearer %s", createJwtToken(queryHash));
    }

    String createQueryHash(String queryString) {
        try {
            MessageDigest md = MessageDigest.getInstance(QUERY_HASH_ALGORITHM);
            md.update(queryString.getBytes("utf8"));
            return String.format("%0128x", new BigInteger(1, md.digest()));
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.upbit.com/v1/orderbook";
        String queryString = "market=" + asset.getSymbol();
        String queryHash = createQueryHash(queryString);
        Request request = new Request.Builder()
                .url(url + "?" + queryString)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", createAuthorizationHeader(queryHash))
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            checkResponseIsError(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String responseBody;
        List<ValueMap> rows;
        try {
            responseBody = Objects.requireNonNull(response.body()).string();
            rows = objectMapper.readValue(responseBody, new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ValueMap row = rows.get(0);
        return OrderBook.builder()
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
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.upbit.com/v1/candles/";
        switch(ohlcvType) {
            case MINUTE -> url += "minutes/1";
            case DAILY -> url += "days";
            default -> throw new RuntimeException("invalid OhlcvType");
        }
        String queryString = "market=" + asset.getSymbol() +
                "&count=200";
        String queryHash = createQueryHash(queryString);
        Request request = new Request.Builder()
                .url(url + "?" + queryString)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", createAuthorizationHeader(queryHash))
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            checkResponseIsError(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String responseBody;
        List<ValueMap> rows;
        try {
            responseBody = Objects.requireNonNull(response.body()).string();
            rows = objectMapper.readValue(responseBody, new TypeReference<>(){});
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
                            .ohlcvType(OhlcvType.MINUTE)
                            .dateTime(dateTime)
                            .openPrice(row.getNumber("opening_price"))
                            .highPrice(row.getNumber("high_price"))
                            .lowPrice(row.getNumber("row_price"))
                            .closePrice(row.getNumber("trade_price"))
                            .volume(row.getNumber("candle_acc_trade_volume"))
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Override
    public Balance getBalance() throws InterruptedException {
        return null;
    }

    @Override
    public void buyAsset(TradeAsset tradeAsset, OrderType orderType, Integer quantity, BigDecimal price) throws InterruptedException {
        // TODO calculates price from order book and quantity
        order(tradeAsset.getSymbol(), "bid", "price", price, null);
    }

    @Override
    public void sellAsset(BalanceAsset balanceAsset, OrderType orderType, Integer quantity, BigDecimal price) throws InterruptedException {
        order(balanceAsset.getSymbol(), "ask", "market", null, quantity);
    }

    private void order(String market, String side, String ordType, BigDecimal price, Integer volume) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.upbit.com/v1/orders";
        ValueMap payloadMap = new ValueMap(){{
            put("market", market);
            put("side", side);
            put("ord_type", ordType);
            put("price", price);
            put("volume", volume);
        }};
        String payload = null;
        try {
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String queryHash = createQueryHash(payload);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", createAuthorizationHeader(queryHash))
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            checkResponseIsError(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String responseBody;
        List<ValueMap> rows;
        try {
            responseBody = Objects.requireNonNull(response.body()).string();
            rows = objectMapper.readValue(responseBody, new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkResponseIsError(Response response) throws RuntimeException {
        if(!response.isSuccessful()) {
            String errorMessage = null;
            try {
                errorMessage = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                errorMessage = e.getMessage();
            }
            throw new RuntimeException(errorMessage);
        }
    }

}
