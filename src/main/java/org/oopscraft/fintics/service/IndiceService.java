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
            indiceIndicators.add(getIndiceIndicator(symbol));
        }
        return indiceIndicators;
    }

    public IndiceIndicator getIndiceIndicator(IndiceSymbol symbol) {

        // minute ohlcv
        LocalDateTime minuteMaxDateTime = indiceOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(symbol, OhlcvType.MINUTE)
                .orElse(LocalDateTime.now());
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.MINUTE, minuteMaxDateTime.minusDays(1), minuteMaxDateTime).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyMaxDateTime = indiceOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(symbol, OhlcvType.DAILY)
                .orElse(LocalDateTime.now());
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, OhlcvType.DAILY, dailyMaxDateTime.minusMonths(3), dailyMaxDateTime).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // return indicator
        return IndiceIndicator.builder()
                .symbol(symbol)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build();
    }


}
