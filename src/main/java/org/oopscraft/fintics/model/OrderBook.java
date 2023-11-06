package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderBook {

    private BigDecimal price;

    private BigDecimal bidPrice;

    private BigDecimal askPrice;

}
