package org.oopscraft.fintics.client.kis;

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

    public synchronized static KisAccessToken getAccessToken(String apiUrl, String appKey, String appSecret) {
        KisAccessToken accessToken = accessTokens.stream()
                .filter(element ->
                        element.getApiUrl().equals(apiUrl)
                        && element.getAppKey().equals(appKey)
                        && element.getAppSecret().equals(appSecret))
                .findFirst()
                .orElse(null);

        if(accessToken == null || accessToken.isExpired()) {
            accessToken = refreshAccessToken(apiUrl, appKey, appSecret);
            accessTokens.add(accessToken);
        }

        return accessToken;
    }

    private synchronized static KisAccessToken refreshAccessToken(String apiUrl, String appKey, String appSecret) {
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
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        String accessToken = responseMap.getString("access_token");
        LocalDateTime accessTokenExpiredDateTime = LocalDateTime.parse(
                responseMap.getString("access_token_token_expired"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        return KisAccessToken.builder()
                .apiUrl(apiUrl)
                .appKey(appKey)
                .appSecret(appSecret)
                .accessToken(accessToken)
                .expireDateTime(accessTokenExpiredDateTime)
                .build();
    }

}
