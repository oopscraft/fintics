package org.oopscraft.fintics.client.asset.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UsAssetClient extends AssetClient {

    private static final String MARKET_US = "US";

    private static final Currency CURRENCY_USD = Currency.getInstance("USD");

    private final ObjectMapper objectMapper;

    public UsAssetClient(AssetClientProperties assetClientProperties, ObjectMapper objectMapper) {
        super(assetClientProperties);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssets("NASDAQ"));
        assets.addAll(getStockAssets("NYSE"));
        assets.addAll(getStockAssets("AMEX"));
        assets.addAll(getEtfAssets());
        return assets;
    }

    @Override
    public boolean isSupported(Asset asset) {
        return asset.getAssetId().startsWith("US.");
    }

    @Override
    public List<AssetMeta> getAssetMetas(Asset asset) {
        return getStockAssetMetas(asset);
    }

    /**
     * gets stock assets
     * @param exchange exchange code
     * @return list of stock asset
     */
    List<Asset> getStockAssets(String exchange) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();
        String url = String.format("https://api.nasdaq.com/api/screener/stocks?tableonly=true&download=true&exchange=%s", exchange);
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rowsNode = rootNode.path("data").path("rows");
        List<Map<String, String>> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o1.get("marketCap"), "0"));
            BigDecimal o2MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o2.get("marketCap"),"0"));
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // return
        return rows.stream()
                .map(row -> {
                    String exchangeMic = null;
                    switch (exchange) {
                        case "NASDAQ" -> exchangeMic = "XNAS";
                        case "NYSE" -> exchangeMic = "XNYS";
                        case "AMEX" -> exchangeMic = "XASE";
                    }
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_US, row.get("symbol")))
                            .assetName(row.get("name"))
                            .market(MARKET_US)
                            .exchange(exchangeMic)
                            .type("STOCK")
                            .marketCap(new BigDecimal(StringUtils.defaultIfBlank(row.get("marketCap"), "0")))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * https://www.nasdaq.com/market-activity/etf/screener
     * @return list of etf asset
     */
    protected List<Asset> getEtfAssets() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();
        String url = "https://api.nasdaq.com/api/screener/etf?download=true";
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rowsNode = rootNode.path("data").path("data").path("rows");
        List<Map<String, String>> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o1.get("lastSalePrice"),"$0").replace("$",""));
            BigDecimal o2LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o2.get("lastSalePrice"),"$0").replace("$",""));
            return o2LastSalePrice.compareTo(o1LastSalePrice);
        });

        List<Asset> assets = rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(MARKET_US, row.get("symbol")))
                        .assetName(row.get("companyName"))
                        .market(MARKET_US)
                        .type("ETF")
                        .build())
                .collect(Collectors.toList());

        // fill exchange
        List<String> symbols = assets.stream().map(Asset::getSymbol).toList();
        Map<String, String> exchangeMap = getExchangeMap(symbols);
        assets.forEach(asset -> asset.setExchange(exchangeMap.get(asset.getSymbol())));

        // return
        return assets;
    }

    /**
     * gets exchange map
     * @param symbols list of symbols to retrieve
     * @return exchange map
     */
    Map<String, String> getExchangeMap(List<String> symbols) {
        Map<String, String> exchangeMicMap = new LinkedHashMap<>();
        final int BATCH_SIZE = 100;
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .readTimeout(10_000)
                    .build();
            HttpHeaders headers = createYahooHeader();
            for (int i = 0; i < symbols.size(); i += BATCH_SIZE) {
                List<String> batchSymbols = symbols.subList(i, Math.min(i + BATCH_SIZE, symbols.size()));
                String symbolParam = String.join(",", batchSymbols);
                String url = String.format("https://query2.finance.yahoo.com/v1/finance/quoteType/?symbol=%s&lang=en-US&region=US", symbolParam);
                RequestEntity<Void> requestEntity = RequestEntity.get(url)
                        .headers(headers)
                        .build();
                String responseBody = restTemplate.exchange(requestEntity, String.class).getBody();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultNode = rootNode.path("quoteType").path("result");
                List<Map<String, String>> results = objectMapper.convertValue(resultNode, new TypeReference<>() {});
                for (Map<String, String> result : results) {
                    String symbol = result.get("symbol");
                    String exchange = result.get("exchange");
                    String exchangeMic = switch (exchange) {
                        case "NGM" -> "XNAS";
                        case "PCX" -> "XASE";
                        // BATS Exchange to BATS (currently Cboe BZX Exchange)
                        case "BTS" -> "BATS";
                        default -> "XNYS";
                    };
                    exchangeMicMap.put(symbol, exchangeMic);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching exchange information", e);
        }
        // return
        return exchangeMicMap;
    }

    /**
     * gets stock asset metas
     * @param asset asset
     * @return list of asset meta
     */
    List<AssetMeta> getStockAssetMetas(Asset asset) {
        try {
            BigDecimal totalAssets = null;
            BigDecimal totalEquity = null;
            BigDecimal netIncome = null;
            BigDecimal eps = null;
            BigDecimal per = null;
            BigDecimal roe = null;
            BigDecimal roa = null;
            BigDecimal dividendYield = null;

            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .readTimeout(30_000)
                    .build();
            HttpHeaders headers = createNasdaqHeaders();

            // calls summary api
            String summaryUrl = String.format(
                    "https://api.nasdaq.com/api/quote/%s/summary?assetclass=stocks",
                    asset.getSymbol()
            );
            RequestEntity<Void> summaryRequestEntity = RequestEntity.get(summaryUrl)
                    .headers(headers)
                    .build();
            ResponseEntity<String> summaryResponseEntity = restTemplate.exchange(summaryRequestEntity, String.class);
            JsonNode summaryRootNode;
            try {
                summaryRootNode = objectMapper.readTree(summaryResponseEntity.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode summaryDataNode = summaryRootNode.path("data").path("summaryData");
            HashMap<String, Map<String,String>> summaryDataMap = objectMapper.convertValue(summaryDataNode, new TypeReference<>() {});

            // price, market cap
            for(String name : summaryDataMap.keySet()) {
                Map<String, String> map = summaryDataMap.get(name);
                String value = map.get("value");
                if (name.equals("PERatio")) {
                    per = new BigDecimal(value);
                }
                if(name.equals("EarningsPerShare")) {
                    eps = convertCurrencyToNumber(value);
                }
                if(name.equals("Yield")) {
                    dividendYield = convertPercentageToNumber(value);
                }
            }

            // calls financial api
            String financialUrl = String.format(
                    "https://api.nasdaq.com/api/company/%s/financials?frequency=1", // frequency 2 is quarterly
                    asset.getSymbol()
            );
            RequestEntity<Void> financialRequestEntity = RequestEntity.get(financialUrl)
                    .headers(headers)
                    .build();
            ResponseEntity<String> financialResponseEntity = restTemplate.exchange(financialRequestEntity, String.class);
            JsonNode financialRootNode;
            try {
                financialRootNode = objectMapper.readTree(financialResponseEntity.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode balanceSheetTableRowsNode = financialRootNode.path("data").path("balanceSheetTable").path("rows");
            List<Map<String,String>> balanceSheetTableRows = objectMapper.convertValue(balanceSheetTableRowsNode, new TypeReference<>(){});
            JsonNode incomeStatementTableRowsNode = financialRootNode.path("data").path("incomeStatementTable").path("rows");
            List<Map<String,String>> incomeStatementTableRows = objectMapper.convertValue(incomeStatementTableRowsNode, new TypeReference<>(){});

            for(Map<String,String> row : balanceSheetTableRows) {
                String key = row.get("value1");
                String value = row.get("value2");
                if("Total Equity".equals(key)) {
                    totalEquity = convertCurrencyToNumber(value);
                }
                if("Total Assets".equals(key)) {
                    totalAssets = convertCurrencyToNumber(value);
                }
            }

            for(Map<String,String> row : incomeStatementTableRows) {
                String key = row.get("value1");
                String value = row.get("value2");
                if("Net Income".equals(key)) {
                    netIncome = convertCurrencyToNumber(value);
                }
            }

            // roe
            if(netIncome != null && totalEquity != null) {
                roe = netIncome.divide(totalEquity, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // roa
            if(netIncome != null && totalAssets != null) {
                roa = netIncome.divide(totalAssets, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // return
            String assetId = asset.getAssetId();
            Instant dateTime = Instant.now();
            return List.of(
                    AssetMeta.builder()
                            .assetId(assetId)
                            .name("PER")
                            .value(Objects.toString(per))
                            .dateTime(dateTime)
                            .build(),
                    AssetMeta.builder()
                            .assetId(assetId)
                            .name("ROE")
                            .value(Objects.toString(roe))
                            .dateTime(dateTime)
                            .build(),
                    AssetMeta.builder()
                            .assetId(assetId)
                            .name("ROA")
                            .value(Objects.toString(roa))
                            .dateTime(dateTime)
                            .build(),
                    AssetMeta.builder()
                            .assetId(assetId)
                            .name("Dividend Yield")
                            .value(Objects.toString(dividendYield))
                            .dateTime(dateTime)
                            .build()
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates nasdaq http headers
     * @return http headers
     */
    HttpHeaders createNasdaqHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority","api.nasdaq.com");
        headers.add("origin","https://www.nasdaq.com");
        headers.add("referer","https://www.nasdaq.com");
        headers.add("sec-ch-ua","\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"");
        headers.add("sec-ch-ua-mobile","?0");
        headers.add("sec-ch-ua-platform", "macOS");
        headers.add("sec-fetch-dest","empty");
        headers.add("sec-fetch-mode","cors");
        headers.add("sec-fetch-site", "same-site");
        headers.add("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * creates yahoo finance http headers
     * @return http headers
     */
    HttpHeaders createYahooHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority"," query1.finance.yahoo.com");
        headers.add("Accept", "*/*");
        headers.add("origin", "https://finance.yahoo.com");
        headers.add("referer", "");
        headers.add("Sec-Ch-Ua","\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"");
        headers.add("Sec-Ch-Ua-Mobile","?0");
        headers.add("Sec-Ch-Ua-Platform", "macOS");
        headers.add("Sec-Fetch-Dest","document");
        headers.add("Sec-Fetch-Mode","navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * converts currency string to number
     * @param value currency string
     * @return currency number
     */
    BigDecimal convertCurrencyToNumber(String value) {
        if(value != null) {
            value = value.replace(CURRENCY_USD.getSymbol(), "");
            value = value.replace(",","");
            return new BigDecimal(value);
        }
        return null;
    }

    /**
     * converts percentage string to number
     * @param value percentage string
     * @return percentage number
     */
    BigDecimal convertPercentageToNumber(String value) {
        value = value.replace("%", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

}
