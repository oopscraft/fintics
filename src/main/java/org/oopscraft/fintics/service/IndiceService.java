package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndiceService {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    public List<IndiceIndicator> getIndiceIndicators(LocalDateTime baseDateTime) {
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            indiceIndicators.add(getIndiceIndicator(symbol, baseDateTime));
        }
        return indiceIndicators;
    }

    public IndiceIndicator getIndiceIndicator(IndiceSymbol symbol, LocalDateTime baseDateTime) {
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.MINUTE, baseDateTime.minusDays(1), baseDateTime).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.DAILY, baseDateTime.minusMonths(1), baseDateTime).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
        return IndiceIndicator.builder()
                .symbol(symbol)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build();
    }


}
