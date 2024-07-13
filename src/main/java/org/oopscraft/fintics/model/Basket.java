package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.BasketEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Basket {

    private String basketId;

    private String basketName;

    private String market;

    private boolean changeEnabled;

    private String changeSchedule;

    private Language language;

    private String script;

    public static enum Language {
        GROOVY, PYTHON
    }

    @Builder.Default
    private List<BasketAsset> basketAssets = new ArrayList<>();

    public static Basket from(BasketEntity basketEntity) {
        return Basket.builder()
                .basketId(basketEntity.getBasketId())
                .basketName(basketEntity.getBasketName())
                .market(basketEntity.getMarket())
                .changeEnabled(basketEntity.isChangeEnabled())
                .changeSchedule(basketEntity.getChangeSchedule())
                .language(basketEntity.getLanguage())
                .script(basketEntity.getScript())
                .basketAssets(basketEntity.getBasketAssets().stream()
                        .map(BasketAsset::from)
                        .toList())
                .build();
    }

}
