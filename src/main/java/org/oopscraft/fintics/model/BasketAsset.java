package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAsset {

    private String symbol;

    private String name;

}
