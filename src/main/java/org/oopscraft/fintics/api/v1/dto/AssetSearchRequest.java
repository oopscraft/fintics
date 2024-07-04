package org.oopscraft.fintics.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSearchRequest {

    private String assetId;

    private String assetName;

    private String market;

    private Boolean favorite;

    private BigDecimal perFrom;

    private BigDecimal perTo;

    private BigDecimal roeFrom;

    private BigDecimal roeTo;

    private BigDecimal roaFrom;

    private BigDecimal roaTo;

}
