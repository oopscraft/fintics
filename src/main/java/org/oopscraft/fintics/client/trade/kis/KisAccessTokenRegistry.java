package org.oopscraft.fintics.client.trade.kis;

import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
            } catch(Throwable e) {
                // 토큰 발급 자체도 1분당 1회발급 제약에 걸리게 됨으로
                // 오류 발생 시에는 1분(이상) 호출 자체를 하지 않아야 되므로
                // TEMP_ERROR_TOKEN 으로 재발급 요청 없이 오류만 발생 하도록 처리
                accessToken = KisAccessToken.builder()
                        .apiUrl(apiUrl)
                        .appKey(appKey)
                        .appSecret(appSecret)
                        .accessToken("TEMP_ERROR_TOKEN")
                        .expireDateTime(LocalDateTime.now().plusMinutes(2))     // 만료 시간 2분 후로 설정(2분후 만료 됨으로 재발급 요청됨)
                        .build();
            }
            accessTokens.add(accessToken);
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
        LocalDateTime expiredDateTime = LocalDateTime.parse(
                responseMap.getString("access_token_token_expired"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        return KisAccessToken.builder()
                .apiUrl(apiUrl)
                .appKey(appKey)
                .appSecret(appSecret)
                .accessToken(accessToken)
                .expireDateTime(expiredDateTime)
                .build();
    }

}
