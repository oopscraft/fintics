package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.BasketEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Basket {

    private String basketId;

    private String basketName;

    private String market;

    private boolean rebalanceEnabled;

    private String rebalanceSchedule;

    private Language language;

    private String script;

    public static enum Language {
        GROOVY, PYTHON
    }

    @Builder.Default
    private List<BasketAsset> basketAssets = new ArrayList<>();

    /**
     * gets specified basket asset
     * @param assetId asset id
     * @return basket asset
     */
    public Optional<BasketAsset> getBasketAsset(String assetId) {
        return basketAssets.stream()
                .filter(it -> Objects.equals(it.getAssetId(), assetId))
                .findFirst();
    }

    public static Basket from(BasketEntity basketEntity) {
        return Basket.builder()
                .basketId(basketEntity.getBasketId())
                .basketName(basketEntity.getBasketName())
                .market(basketEntity.getMarket())
                .rebalanceEnabled(basketEntity.isRebalanceEnabled())
                .rebalanceSchedule(basketEntity.getRebalanceSchedule())
                .language(basketEntity.getLanguage())
                .script(basketEntity.getScript())
                .basketAssets(basketEntity.getBasketAssets().stream()
                        .map(BasketAsset::from)
                        .toList())
                .build();
    }

}
