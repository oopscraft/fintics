package org.oopscraft.fintics.client.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KisClient implements Client {

    private final KisClientProperties properties;

    private final ObjectMapper objectMapper;

    String getAccessKey() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        ValueMap payloadMap = new ValueMap(){{
            put("grant_type","client_credentials");
            put("appkey", properties.getAppKey());
            put("appsecret", properties.getAppSecret());
        }};
        RequestEntity<Map<String,Object>> requestEntity = RequestEntity
                .post(properties.getApiUrl() + "/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        return responseMap.getString("access_token");
    }

    HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("authorization", "Bearer " + getAccessKey());
        httpHeaders.add("appkey", properties.getAppKey());
        httpHeaders.add("appsecret", properties.getAppSecret());
        return httpHeaders;
    }

    @Override
    public Balance getBalance() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = properties.getApiUrl() + "/uapi/domestic-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = properties.isProduction() ? "TTTC8424R" : "VTTC8434R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", properties.getAccountNo().split("-")[0])
                .queryParam("ACNT_PRDT_CD", properties.getAccountNo().split("-")[1])
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
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode output1Node = rootNode.path("output1");
        List<ValueMap> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});

        JsonNode output2Node = rootNode.path("output2");
        List<ValueMap> output2 = objectMapper.convertValue(output2Node, new TypeReference<List<ValueMap>>(){});

        List<BalanceAsset> assets = output1.stream()
                .map(row -> {
                    return BalanceAsset.builder()
                            .symbol(row.getString("pdno"))
                            .name(row.getString("prdt_name"))
                            .quantity(row.getNumber("hldg_qty"))
                            .buyPrice(row.getNumber("pchs_avg_pric"))
                            .build();
                })
                .collect(Collectors.toList());

        return Balance.builder()
                .cash(output2.get(0).getNumber("dnca_tot_amt"))
                .assets(assets)
                .build();
    }

    @Override
    public AssetIndicator getAssetIndicator(Asset asset) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = properties.getApiUrl() + "/uapi/domestic-stock/v1/quotations/inquire-price";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010100");
        String fidCondMrktDivCode;
        String fidInputIscd;
        switch(asset.getType()) {
            case STOCK:
                fidCondMrktDivCode = "J";
                fidInputIscd = asset.getSymbol();
                break;
            case ETF:
                fidCondMrktDivCode = "ETF";
                fidInputIscd = asset.getSymbol();
                break;
            case ETN:
                fidCondMrktDivCode = "ETN";
                fidInputIscd = "Q" + asset.getSymbol();
                break;
            default:
                throw new RuntimeException("invalid asset type - " + asset.getType());
        }
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg);
        }

        ValueMap output = objectMapper.convertValue(rootNode.path("output"), ValueMap.class);

        return AssetIndicator.builder()
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .price(output.getNumber("stck_prpr"))
                .build();
    }


    @Override
    public void buyBasketAsset(BasketAsset basketAsset, int quantity, BigDecimal price) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = properties.getApiUrl() + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = properties.isProduction() ? "TTTC0802U" : "VTTC9802U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", properties.getAccountNo().split("-")[0]);
            put("ACNT_PRDT_CD", properties.getAccountNo().split("-")[1]);
            put("PDNO", basketAsset.getSymbol());
            put("ORD_DVSN", "00");
            put("ORD_QTY", quantity);
            put("ORD_UNPR", price);
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg = responseMap.getString("msg");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public void sellBalanceAsset(BalanceAsset balanceAsset, int quantity, BigDecimal price) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = properties.getApiUrl() + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = properties.isProduction() ? "TTTC0801U" : "VTTC0801U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", properties.getAccountNo().split("-")[0]);
            put("ACNT_PRDT_CD", properties.getAccountNo().split("-")[1]);
            put("PDNO", balanceAsset.getSymbol());
            put("ORD_DVSN", "00");
            put("ORD_QTY", quantity);
            put("ORD_UNPR", price);
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg = responseMap.getString("msg");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg);
        }
    }

}
