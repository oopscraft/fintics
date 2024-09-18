package org.oopscraft.fintics.client.asset.market;

import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class KrAssetClient extends AssetClient {

    private static final String MARKET_KR = "KR";

    public KrAssetClient(AssetClientProperties assetClientProperties) {
        super(assetClientProperties);
    }

    /**
     * returns assets to trade
     * @return assets
     */
    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssetsByExchangeType("11")); // kospi
        assets.addAll(getStockAssetsByExchangeType("12")); // kosdaq
        assets.addAll(getEtfAssets());  // ETF
        return assets;
    }

    @Override
    public boolean isSupported(Asset asset) {
        return asset.getAssetId().startsWith("KR.");
    }

    @Override
    public List<AssetMeta> getAssetMetas(Asset asset) {
        if ("STOCK".equals(asset.getType())) {
            return getStockAssetMetas(asset);
        }
        return new ArrayList<>();
    }

    /**
     * returns asset list by exchange type
     * 코스피, 코스닥 여부에 따라 다른 부분이 존재 함.
     * @param exchangeType exchange type
     * @return assets
     */
    List<Asset> getStockAssetsByExchangeType(String exchangeType) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";

        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02004V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "SecnIssuPListEL1";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnIssuPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","41");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,reset,reset,seach,favorites,xls,link,link,wide,wide,top,");
            put("FICS_CD", "");
            put("INDTP_CLSF_NO", "");
            put("STD_DT", "20230922");
            put("CALTOT_MART_TPCD", exchangeType);
            put("SECN_KACD", "99");
            put("AG_ORG_TPCD", "99");
            put("SETACC_MMDD", "99");
            put("ISSU_FORM", "");
            put("ORDER_BY", "TR_QTY");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String exchange;
        switch(exchangeType) {
            case "11" -> exchange = "XKRX";
            case "12" -> exchange = "XKOS";
            default -> throw new RuntimeException("invalid exchange type");
        }

        return rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                        .name(row.get("KOR_SECN_NM"))
                        .market(MARKET_KR)
                        .exchange(exchange)
                        .type("STOCK")
                        .marketCap(toNumber(row.get("MARTP_TOTAMT"), null))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * returns ETF assets
     * @return ETF assets
     */
    List<Asset> getEtfAssets() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";

        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06025V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "secnIssuStatPList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfSetredInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","174");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,detail,seach,searchIcon,comparison,link,link,wide,wide,top,");
            put("mngco", "");
            put("SETUP_DT", "");
            put("from_TOT_RECM_RATE", "");
            put("to_TOT_RECM_RATE", "");
            put("from_NETASST_TOTAMT", "");
            put("to_NETASST_TOTAMT", "");
            put("kor_SECN_NM", "");
            put("ic4_select", "2");
            put("select_sorting", "2");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String exchange = "XKRX";

        // convert assets
        return rows.stream()
                .map(row -> {
                    // market cap (etf is 1 krw unit)
                    BigDecimal marketCap = toNumber(row.get("NETASST_TOTAMT"), null);
                    if(marketCap != null) {
                        marketCap = marketCap.divide(BigDecimal.valueOf(100_000_000), MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                    }

                    // return
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market(MARKET_KR)
                            .exchange(exchange)
                            .type("ETF")
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * returns asset meta list
     * @param asset asset
     * @return list of meta
     */
    List<AssetMeta> getStockAssetMetas(Asset asset) {
        BigDecimal per = null;
        BigDecimal roe = null;
        BigDecimal roa = null;
        BigDecimal dividendYield = null;

        // check stock
        if (!Objects.equals(asset.getType(), "STOCK")) {
            return new ArrayList<>();
        }

        // request template
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";

        // sec info
        Map<String,String> secInfo = getSecInfo(asset.getSymbol());
        String isin = secInfo.get("ISIN");
        String shotnIsin = secInfo.get("SHOTN_ISIN");
        String issucoCustno = secInfo.get("ISSUCO_CUSTNO");

        // calls service 1
        try {
            String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
            HttpHeaders headers = createSeibroHeaders(w2xPath);
            headers.setContentType(MediaType.APPLICATION_XML);
            String action = "indtpSincView";
            String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("ISIN", isin);
                put("SHOTN_ISIN", shotnIsin);
                put("ISSUCO_CUSTNO", issucoCustno);
            }};
            String payloadXml = createPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            // exchange
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            // response
            String responseBody = responseEntity.getBody();
            List<Map<String,String>> responseList = convertXmlToList(responseBody);
            // find value
            for(Map<String,String> row : responseList) {
                String name = row.get("HB");
                String value = row.get("CO_VALUE");
                if (name.startsWith("PER")) {
                    per = toNumber(value, null);
                }
                if (name.startsWith("ROE")) {
                    roe = toNumber(value, null);
                }
                if (name.startsWith("배당")) {
                    dividendYield = toNumber(value, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // calls service 2
        try {
            String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
            HttpHeaders headers = createSeibroHeaders(w2xPath);
            headers.setContentType(MediaType.APPLICATION_XML);
            String action = "finaRatioList";
            String task = "ksd.safe.bip.cnts.Company.process.EntrBySecIssuPTask";
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("ISIN", isin);
                put("SHOTN_ISIN", shotnIsin);
                put("ISSUCO_CUSTNO", issucoCustno);
                put("STD_DT", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                put("SETACC_YYMM3", now.minusYears(1).format(DateTimeFormatter.ofPattern("yyyy")));
            }};
            String payloadXml = createPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            // exchange
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            // response
            String responseBody = responseEntity.getBody();
            List<Map<String,String>> responseList = convertXmlToList(responseBody);
            // find value
            for(Map<String,String> row : responseList) {
                String name = row.get("HB");
                String value = row.get("A3");
                if (name.startsWith("ROA")) {
                    roa = toNumber(value, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // return
        String assetId = asset.getAssetId();
        Instant dateTime = Instant.now();
        return List.of(
                AssetMeta.builder()
                        .assetId(assetId)
                        .name("PER")
                        .value(Objects.toString(per, null))
                        .dateTime(dateTime)
                        .build(),
                AssetMeta.builder()
                        .assetId(assetId)
                        .name("ROE")
                        .value(Objects.toString(roe, null))
                        .dateTime(dateTime)
                        .build(),
                AssetMeta.builder()
                        .assetId(assetId)
                        .name("ROA")
                        .value(Objects.toString(roa, null))
                        .dateTime(dateTime)
                        .build(),
                AssetMeta.builder()
                        .assetId(assetId)
                        .name("Dividend Yield")
                        .value(Objects.toString(dividendYield, null))
                        .dateTime(dateTime)
                        .build()
        );
    }

    /**
     * gets SEC info
     * @param symbol symbol
     * @return sec info
     */
    Map<String, String> getSecInfo(String symbol) {
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

    /**
     * returns seibro api header
     * @param w2xPath w2xPath
     * @return http headers for seibro
     */
    HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
    }

    /**
     * creates payload XML string
     * @param action seibro api action
     * @param task seibro api task
     * @param payloadMap payload map
     * @return payload XML string
     */
    String createPayloadXml(String action, String task, Map<String,String> payloadMap) {
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

    /**
     * convert seibro xml response to map
     * @param responseXml response xml
     * @return map
     */
    Map<String, String> convertXmlToMap(String responseXml) {
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

    /**
     * convert seibro response XML to list
     * @param responseXml response XML
     * @return list of seibro response map
     */
    public static List<Map<String, String>> convertXmlToList(String responseXml) {
        List<Map<String,String>> list = new ArrayList<>();
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

            Double count = (Double) xPath.evaluate("count(//vector)", document, XPathConstants.NUMBER);
            if(count.intValue() == 0) {
                throw new RuntimeException("response body error - vector element count is 0.");
            }

            XPathExpression expr = xPath.compile("//vector/data/result");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Map<String, String> map = new LinkedHashMap<>();
                Node result = nodeList.item(i);
                NodeList propertyNodes = result.getChildNodes();
                for(int ii = 0; ii < propertyNodes.getLength(); ii++) {
                    Element propertyElement = (Element) propertyNodes.item(ii);
                    String propertyName = propertyElement.getTagName();
                    String propertyValue = propertyElement.getAttribute("value");
                    map.put(propertyName, propertyValue);
                }
                list.add(map);
            }

        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * convert to string to number
     * @param value number string
     * @param defaultValue default number
     * @return converted number
     */
    BigDecimal toNumber(Object value, BigDecimal defaultValue) {
        try {
            String valueString = value.toString().replace(",", "");
            return new BigDecimal(valueString);
        }catch(Throwable e){
            return defaultValue;
        }
    }

}
