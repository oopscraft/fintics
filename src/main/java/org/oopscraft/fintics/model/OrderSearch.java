package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSearch {

    private String tradeId;

    private String symbol;

    private OrderKind orderKind;

    private OrderResult orderResult;

}
