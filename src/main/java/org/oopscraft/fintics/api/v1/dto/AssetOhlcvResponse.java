package org.oopscraft.fintics.api.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Ohlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class AssetOhlcvResponse extends OhlcvResponse {

    private String brokerId;

    private String assetId;
    
    public static AssetOhlcvResponse from(String brokerId, String assetId, Ohlcv ohlcv) {
        return AssetOhlcvResponse.builder()
                .brokerId(brokerId)
                .assetId(assetId)
                .type(ohlcv.getType())
                .dateTime(ohlcv.getDateTime())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getOpenPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

}
