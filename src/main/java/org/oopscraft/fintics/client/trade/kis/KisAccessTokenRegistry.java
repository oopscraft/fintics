package org.oopscraft.fintics.client.trade.kis;

import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class KisAccessTokenRegistry {

    private static final Set<KisAccessToken> accessTokens = new CopyOnWriteArraySet<>();

    public synchronized static KisAccessToken getAccessToken(String apiUrl, String appKey, String appSecret) throws InterruptedException {
        KisAccessToken accessToken = accessTokens.stream()
                .filter(element ->
                        element.getApiUrl().equals(apiUrl)
                        && element.getAppKey().equals(appKey)
                        && element.getAppSecret().equals(appSecret))
                .findFirst()
                .orElse(null);

        if(accessToken == null || accessToken.isExpired()) {
            try {
                accessToken = refreshAccessToken(apiUrl, appKey, appSecret);
                accessTokens.add(accessToken);
            }catch(Throwable e){
                // 토큰 발급 오류 시 1분 sleep 하지 않으면 시도 자체도 카운트 됨으로 계속 오류 발생함.
                Thread.sleep(60_000 + 10_000);
                throw e;
            }
        }

        return accessToken;
    }

    private synchronized static KisAccessToken refreshAccessToken(String apiUrl, String appKey, String appSecret) throws InterruptedException {
        log.info("Refresh Access Token - {}", apiUrl);
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        ValueMap payloadMap = new ValueMap(){{
            put("grant_type","client_credentials");
            put("appkey", appKey);
            put("appsecret", appSecret);
        }};
        RequestEntity<Map<String,Object>> requestEntity = RequestEntity
                .post(apiUrl + "/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        Thread.sleep(1_000);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        String accessToken = responseMap.getString("access_token");
        LocalDateTime expiredDateTime = LocalDateTime.now().plusHours(1);

        return KisAccessToken.builder()
                .apiUrl(apiUrl)
                .appKey(appKey)
                .appSecret(appSecret)
                .accessToken(accessToken)
                .expireDateTime(expiredDateTime)
                .build();
    }

}
