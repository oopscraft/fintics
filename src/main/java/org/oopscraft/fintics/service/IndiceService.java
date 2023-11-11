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

    public List<IndiceIndicator> getIndiceIndicators() {
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            indiceIndicators.add(getIndiceIndicator(symbol).orElseThrow());
        }
        return indiceIndicators;
    }

    public Optional<IndiceIndicator> getIndiceIndicator(IndiceSymbol symbol) {
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.MINUTE, now.minusDays(1), now).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.DAILY, now.minusMonths(3), now).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
        IndiceIndicator indiceIndicator = IndiceIndicator.builder()
                .symbol(symbol)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build();
        return Optional.of(indiceIndicator);
    }


}
