package org.oopscraft.fintics.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Basket {

    @Builder.Default
    private List<BasketAsset> assets = new ArrayList<>();

}
