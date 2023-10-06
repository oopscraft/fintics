package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetIndicator {

    private String symbol;

    private String name;

    private BigDecimal price;

}
