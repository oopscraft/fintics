package org.oopscraft.fintics.model.broker;

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

public abstract class KrBrokerClient extends BrokerClient {

    public KrBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssetsByExchangeType("11")); // kospi
        assets.addAll(getStockAssetsByExchangeType("12")); // kosdaq
        assets.addAll(getEtfAssets());  // ETF
        return assets;
    }

    protected List<Asset> getStockAssetsByExchangeType(String exchangeType) {
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
        List<ValueMap> rows = convertXmlToList(responseBody);

        // sort, limit
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.getString("MARTP_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String market = getDefinition().getExchangeId();
        String exchange;
        switch(exchangeType) {
            case "11" -> exchange = "KRX";
            case "12" -> exchange = "KOSDAQ";
            default -> throw new RuntimeException("invalid exchange type");
        }

        return rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(row.getString("SHOTN_ISIN")))
                        .assetName(row.getString("KOR_SECN_NM"))
                        .market(market)
                        .exchange(exchange)
                        .type("STOCK")
                        .dateTime(LocalDateTime.now())
                        .marketCap(toNumber(row.get("MARTP_TOTAMT"), null))
                        .issuedShares(toNumber(row.get("TOT_ISSU_STKQTY"), null))
                        .per(toNumber(row.get("PER"), null))
                        .roe(toNumber(row.get("ROE"), null))
                        .roa(toNumber(row.get("ROA"), null))
                        .build())
                .collect(Collectors.toList());
    }

    protected List<Asset> getEtfAssets() {
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
        List<ValueMap> rows = convertXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String market = getDefinition().getExchangeId();
        String exchange = "KRX";

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
                            .assetId(toAssetId(row.getString("SHOTN_ISIN")))
                            .assetName(row.getString("KOR_SECN_NM"))
                            .market(market)
                            .exchange(exchange)
                            .type("ETF")
                            .dateTime(LocalDateTime.now())
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
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

    public static List<ValueMap> convertXmlToList(String responseXml) {
        List<ValueMap> list = new ArrayList<>();
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
                ValueMap map = new ValueMap();
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

    protected static ValueMap convertXmlToMap(String responseXml) {
        ValueMap map  = new ValueMap();
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

    public static BigDecimal toNumber(Object value, BigDecimal defaultValue) {
        try {
            String valueString = value.toString().replace(",", "");
            return new BigDecimal(valueString);
        }catch(Throwable e){
            return defaultValue;
        }
    }

    protected static String getIsin(String symbol) {
        ValueMap map = getSecInfo(symbol);
        return Optional.ofNullable(map.getString("ISIN"))
                .orElseThrow();
    }

    protected static String getIssucoCustNo(String symbol) {
        ValueMap map = getSecInfo(symbol);
        return Optional.ofNullable(map.getString("ISSUCO_CUSTNO"))
                .orElseThrow();
    }

    protected static ValueMap getSecInfo(String symbol) {
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
     * https://securities.koreainvestment.com/main/customer/notice/Notice.jsp?&cmd=TF04ga000002&currentPage=1&num=39930
     */
    public BigDecimal getPriceTick(Asset asset, BigDecimal price) throws InterruptedException {
        // etf, etn, elw
        if(Arrays.asList("ETF","ETN","ELW").contains(asset.getType())) {
            return BigDecimal.valueOf(5);
        }
        // default fallback (stock)
        BigDecimal priceTick = null;
        if (price.compareTo(BigDecimal.valueOf(2_000)) <= 0) {
            priceTick = BigDecimal.valueOf(1);
        } else if (price.compareTo(BigDecimal.valueOf(5_000)) <= 0) {
            priceTick = BigDecimal.valueOf(5);
        } else if (price.compareTo(BigDecimal.valueOf(20_000)) <= 0) {
            priceTick = BigDecimal.valueOf(10);
        } else if (price.compareTo(BigDecimal.valueOf(50_000)) <= 0) {
            priceTick = BigDecimal.valueOf(50);
        } else if (price.compareTo(BigDecimal.valueOf(200_000)) <= 0) {
            priceTick = BigDecimal.valueOf(100);
        } else if (price.compareTo(BigDecimal.valueOf(500_000)) <= 0) {
            priceTick = BigDecimal.valueOf(500);
        } else {
            priceTick = BigDecimal.valueOf(1_000);
        }
        return priceTick;
    }

}
