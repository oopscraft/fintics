package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderRequest {

    private Order.Type type;

    private String tradeId;

    private String assetId;

    private Order.Kind kind;

    private BigDecimal quantity;

}
