package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RealizedProfitAsset extends Asset {

    private LocalDate date;

    private BigDecimal quantity;

    private BigDecimal purchasePrice;

    private BigDecimal purchaseAmount;

    private BigDecimal disposePrice;

    private BigDecimal disposeAmount;

    private BigDecimal feeAmount;

    private BigDecimal profitAmount;

    private BigDecimal profitPercentage;

}
