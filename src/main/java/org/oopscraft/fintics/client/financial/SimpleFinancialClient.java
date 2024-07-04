package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.asset.AssetFinancialClient;
import org.oopscraft.fintics.client.asset.AssetFinancialClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetFinancial;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
import java.math.RoundingMode;
import java.util.*;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "asset-financial-client.class-name", havingValue="org.oopscraft.fintics.client.asset.SimpleAssetFinancialClient")
@Slf4j
public class SimpleAssetFinancialClient extends AssetFinancialClient {

    private static final Currency CURRENCY_USD = Currency.getInstance("USD");

    private final ObjectMapper objectMapper;

    protected SimpleAssetFinancialClient(AssetFinancialClientProperties financialClientProperties, ObjectMapper objectMapper) {
        super(financialClientProperties);
        this.objectMapper = objectMapper;
    }

    public static BigDecimal convertCurrencyToNumber(String value, Currency currency) {
        if(value != null) {
            value = value.replace(currency.getSymbol(), "");
            value = value.replace(",","");
            return new BigDecimal(value);
        }
        return null;
    }

    public static BigDecimal convertStringToNumber(String value, double defaultValue) {
        value = value.replace(",", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return BigDecimal.valueOf(defaultValue);
        }
    }

    public static BigDecimal convertPercentageToNumber(String value) {
        value = value.replace("%", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

    @Override
    public AssetFinancial getAssetFinancial(Asset asset) {
        if (asset.getAssetId().startsWith("US.")) {
            return getUsAssetFinancial(asset);
        }
        if (asset.getAssetId().startsWith("KR.")) {
            return getKrAssetFinancial(asset);
        }
        throw new UnsupportedOperationException(String.format("not supporting asset[%s]", asset.getAssetId()));
    }

    AssetFinancial getUsAssetFinancial(Asset asset) {
        BigDecimal issuedShares = null;
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
                eps = convertCurrencyToNumber(value, CURRENCY_USD);
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
                totalEquity = convertCurrencyToNumber(value, CURRENCY_USD);
            }
            if("Total Assets".equals(key)) {
                totalAssets = convertCurrencyToNumber(value, CURRENCY_USD);
            }
        }

        for(Map<String,String> row : incomeStatementTableRows) {
            String key = row.get("value1");
            String value = row.get("value2");
            if("Net Income".equals(key)) {
                netIncome = convertCurrencyToNumber(value, CURRENCY_USD);
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

        return AssetFinancial.builder()
                .issuedShares(issuedShares)
                .totalAssets(totalAssets)
                .totalEquity(totalEquity)
                .netIncome(netIncome)
                .eps(eps)
                .per(per)
                .roe(roe)
                .roa(roa)
                .dividendYield(dividendYield)
                .build();
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

    AssetFinancial getKrAssetFinancial(Asset asset) {
        AssetFinancial assetFinancial = AssetFinancial.builder()
                .build();
        return assetFinancial;
    }

    protected static String getIsin(String symbol) {
        Map<String, String> map = getSecInfo(symbol);
        return Optional.ofNullable(map.get("ISIN"))
                .orElseThrow();
    }

    protected static String getIssucoCustNo(String symbol) {
        Map<String, String> map = getSecInfo(symbol);
        return Optional.ofNullable(map.get("ISSUCO_CUSTNO"))
                .orElseThrow();
    }

    protected static Map<String, String> getSecInfo(String symbol) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";

        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "secnInfoDefault";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("SHOTN_ISIN", symbol);
        }};
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        return convertXmlToMap(responseBody);
    }

    private static HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
    }

    protected static Map<String, String> convertXmlToMap(String responseXml) {
        Map<String, String> map  = new LinkedHashMap<>();
        InputSource inputSource;
        StringReader stringReader;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            stringReader = new StringReader(responseXml);
            inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            XPathExpression expr = xPath.compile("/result/*");
            NodeList propertyNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < propertyNodes.getLength(); i++) {
                Element propertyElement = (Element) propertyNodes.item(i);
                String propertyName = propertyElement.getTagName();
                String propertyValue = propertyElement.getAttribute("value");
                map.put(propertyName, propertyValue);
            }
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    protected static String createPayloadXml(String action, String task, Map<String,String> payloadMap) {
        // Create a new Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder ;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
        Document doc = dBuilder.newDocument();

        // Create the root element <reqParam>
        Element reqParamElement = doc.createElement("reqParam");
        doc.appendChild(reqParamElement);

        // Add attributes to <reqParam>
        Attr actionAttr = doc.createAttribute("action");
        actionAttr.setValue(action);
        reqParamElement.setAttributeNode(actionAttr);

        Attr taskAttr = doc.createAttribute("task");
        taskAttr.setValue(task);
        reqParamElement.setAttributeNode(taskAttr);

        // Add child elements to <reqParam>
        for(String key : payloadMap.keySet()) {
            String value = payloadMap.get(key);
            Element childElement = doc.createElement(key);
            Attr attr = doc.createAttribute("value");
            attr.setValue(value);
            childElement.setAttributeNode(attr);
            reqParamElement.appendChild(childElement);
        }

        // convert to string
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(reqParamElement), new StreamResult(writer));
            return writer.toString();
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
