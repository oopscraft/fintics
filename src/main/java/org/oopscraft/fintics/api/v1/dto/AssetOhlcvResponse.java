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
    
    public static AssetOhlcvResponse from(AssetOhlcv indiceOhlcv) {
        return AssetOhlcvResponse.builder()
                .assetId(indiceOhlcv.getAssetId())
                .type(indiceOhlcv.getType())
                .dateTime(indiceOhlcv.getDateTime())
                .openPrice(indiceOhlcv.getOpenPrice())
                .highPrice(indiceOhlcv.getOpenPrice())
                .lowPrice(indiceOhlcv.getLowPrice())
                .closePrice(indiceOhlcv.getClosePrice())
                .volume(indiceOhlcv.getVolume())
                .build();
    }

}
