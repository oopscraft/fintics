package org.oopscraft.fintics.client.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.model.Asset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class UsBrokerClient extends BrokerClient {

    private final ObjectMapper objectMapper;

    public UsBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssets());
        assets.addAll(getEtfAssets());
        return assets;
    }

    protected List<Asset> getStockAssets() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();
        String url = "https://api.nasdaq.com/api/screener/stocks?tableonly=true&download=true";
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
        List<ValueMap> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o1.getString("marketCap"),"0"));
            BigDecimal o2MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o2.getString("marketCap"),"0"));
            return o2MarketCap.compareTo(o1MarketCap);
        });

        return rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(row.getString("symbol")))
                        .assetName(row.getString("name"))
                        .market(getDefinition().getMarket())
                        .exchange("XNAS")
                        .type("STOCK")
                        .dateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

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
        List<ValueMap> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o1.getString("lastSalePrice"),"$0").replace("$",""));
            BigDecimal o2LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o2.getString("lastSalePrice"),"$0").replace("$",""));
            return o2LastSalePrice.compareTo(o1LastSalePrice);
        });

        return rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(row.getString("symbol")))
                        .assetName(row.getString("companyName"))
                        .market(getDefinition().getMarket())
                        .exchange("XNAS")
                        .type("ETF")
                        .dateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    private static HttpHeaders createNasdaqHeaders() {
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

    @Override
    public BigDecimal getPriceTick(Asset asset, BigDecimal price) throws InterruptedException {
        return BigDecimal.valueOf(0.01);
    }

}
