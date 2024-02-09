package org.oopscraft.fintics.api.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetOhlcv;
import org.oopscraft.fintics.model.Ohlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class AssetOhlcvResponse extends OhlcvResponse {

    private String brokerId;

    private String assetId;
    
    public static AssetOhlcvResponse from(AssetOhlcv assetOhlcv) {
        return AssetOhlcvResponse.builder()
                .assetId(assetOhlcv.getAssetId())
                .type(assetOhlcv.getType())
                .dateTime(assetOhlcv.getDateTime())
                .openPrice(assetOhlcv.getOpenPrice())
                .highPrice(assetOhlcv.getOpenPrice())
                .lowPrice(assetOhlcv.getLowPrice())
                .closePrice(assetOhlcv.getClosePrice())
                .volume(assetOhlcv.getVolume())
                .interpolated(assetOhlcv.isInterpolated())
                .build();
    }

}
