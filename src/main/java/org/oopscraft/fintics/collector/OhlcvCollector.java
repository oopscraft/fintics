package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.OhlcvEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class OhlcvCollector extends AbstractCollector {

    protected <T extends OhlcvEntity> List<T> extractNewOrChangedOhlcvEntities(List<T> ohlcvEntities, List<T> previousOhlcvEntities) {
        return ohlcvEntities.stream()
                .filter(ohlcvEntity -> {
                    OhlcvEntity previousOhlcvEntity = previousOhlcvEntities.stream()
                            .filter(item -> item.getDateTime().equals(ohlcvEntity.getDateTime()))
                            .findFirst()
                            .orElse(null);
                    return previousOhlcvEntity == null || !equalsOhlcvContent(ohlcvEntity, previousOhlcvEntity);
                })
                .toList();
    }

    protected boolean equalsOhlcvContent(OhlcvEntity ohlcvEntity, OhlcvEntity previousOhlcvEntity) {
        int priceScale = Math.min(Optional.ofNullable(ohlcvEntity.getClosePrice()).map(BigDecimal::scale).orElse(0), Optional.ofNullable(previousOhlcvEntity.getClosePrice()).map(BigDecimal::scale).orElse(0));
        int volumeScale = Math.min(Optional.ofNullable(ohlcvEntity.getVolume()).map(BigDecimal::scale).orElse(0), Optional.ofNullable(previousOhlcvEntity.getVolume()).map(BigDecimal::scale).orElse(0));

        BigDecimal ourOpenPrice = Optional.ofNullable(ohlcvEntity.getOpenPrice()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourHighPrice = Optional.ofNullable(ohlcvEntity.getHighPrice()).map(highPrice -> highPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourLowPrice = Optional.ofNullable(ohlcvEntity.getLowPrice()).map(lowPrice -> lowPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourClosePrice = Optional.ofNullable(ohlcvEntity.getClosePrice()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourVolume = Optional.ofNullable(ohlcvEntity.getVolume()).map(volume -> volume.setScale(volumeScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);

        BigDecimal theirOpenPrice = Optional.ofNullable(previousOhlcvEntity.getOpenPrice()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirHighPrice = Optional.ofNullable(previousOhlcvEntity.getHighPrice()).map(highPrice -> highPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirLowPrice = Optional.ofNullable(previousOhlcvEntity.getLowPrice()).map(lowPrice -> lowPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirClosePrice = Optional.ofNullable(previousOhlcvEntity.getClosePrice()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirVolume = Optional.ofNullable(previousOhlcvEntity.getVolume()).map(volume -> volume.setScale(volumeScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);

        return ourOpenPrice.compareTo(theirOpenPrice) == 0
                && ourHighPrice.compareTo(theirHighPrice) == 0
                && ourLowPrice.compareTo(theirLowPrice) == 0
                && ourClosePrice.compareTo(theirClosePrice) == 0
                && ourVolume.compareTo(theirVolume) == 0;
    }

}
