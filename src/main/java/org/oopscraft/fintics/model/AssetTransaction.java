package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetTransaction {

    private LocalDateTime dateTime;

    private Double price;

}
