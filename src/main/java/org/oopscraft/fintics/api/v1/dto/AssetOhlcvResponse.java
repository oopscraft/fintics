package org.oopscraft.fintics.api.v1.dto;

import groovy.lang.DelegatesTo;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetOhlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvResponse extends OhlcvResponse {

    private String assetId;

    public static AssetOhlcvResponse from(AssetOhlcv assetOhlcv) {
        return AssetOhlcvResponse.builder()
                .assetId(assetOhlcv.getAssetId())
                .dateTime(assetOhlcv.getDateTime())
                .type(assetOhlcv.getType())
                .openPrice(assetOhlcv.getOpenPrice())
                .highPrice(assetOhlcv.getHighPrice())
                .lowPrice(assetOhlcv.getLowPrice())
                .closePrice(assetOhlcv.getClosePrice())
                .volume(assetOhlcv.getVolume())
                .build();
    }

}
