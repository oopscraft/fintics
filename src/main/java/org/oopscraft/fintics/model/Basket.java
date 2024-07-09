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

    @Builder.Default
    private List<BasketAsset> basketAssets = new ArrayList<>();

    public static Basket from(BasketEntity basketEntity) {
        return Basket.builder()
                .basketId(basketEntity.getBasketId())
                .basketName(basketEntity.getBasketName())
                .basketAssets(basketEntity.getBasketAssets().stream()
                        .map(BasketAsset::from)
                        .toList())
                .build();
    }

}
