package org.oopscraft.fintics.client.asset.market;

import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpbitAssetClient extends AssetClient {

    private final RestTemplate restTemplate;

    public UpbitAssetClient(AssetClientProperties assetClientProperties) {
        super(assetClientProperties);
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Asset> getAssets() {
        RequestEntity<Void> requestEntity = RequestEntity
                .get("https://api.upbit.com/v1/market/all")
                .build();
        ResponseEntity<List<Map<String, String>>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
        return responseEntity.getBody().stream()
                .map(map -> {
                    return Asset.builder()
                            .assetId(toAssetId("UPBIT", map.get("market")))
                            .name(map.get("english_name"))
                            .market("UPBIT")
                            .exchange("UPBIT")
                            .type("CRYPTO")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSupported(Asset asset) {
        return false;
    }

    @Override
    public List<AssetMeta> getAssetMetas(Asset asset) {
        return new ArrayList<>();
    }
}
