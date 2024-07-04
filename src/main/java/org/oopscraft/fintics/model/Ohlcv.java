package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.OhlcvEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcv {

    private String assetId;

    private Type type;

    private Instant datetime;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    public enum Type { MINUTE, DAILY }

    @Converter(autoApply = true)
    public static class TypeConverter extends AbstractEnumConverter<Type> {}

    public static AssetOhlcv of(String assetId, Instant dateTime, double open, double high, double low, double close, double volume) {
        return AssetOhlcv.builder()
                .assetId(assetId)
                .datetime(dateTime)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .volume(BigDecimal.valueOf(volume))
                .build();
    }

    public static AssetOhlcv from(OhlcvEntity assetOhlcvEntity) {
        return AssetOhlcv.builder()
                .assetId(assetOhlcvEntity.getAssetId())
                .type(assetOhlcvEntity.getType())
                .datetime(assetOhlcvEntity.getDatetime())
                .open(assetOhlcvEntity.getOpen())
                .high(assetOhlcvEntity.getHigh())
                .low(assetOhlcvEntity.getLow())
                .close(assetOhlcvEntity.getClose())
                .volume(assetOhlcvEntity.getVolume())
                .build();
    }

}
