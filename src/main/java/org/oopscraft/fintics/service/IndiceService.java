package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndiceService {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    public List<Indice> getIndices() {
        return Arrays.stream(IndiceId.values())
                .map(Indice::from)
                .collect(Collectors.toList());
    }

    public Optional<Indice> getIndice(IndiceId symbol) {
        return Optional.ofNullable(Indice.from(symbol));
    }
    public List<IndiceIndicator> getIndiceIndicators() {
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceId symbol : IndiceId.values()) {
            indiceIndicators.add(getIndiceIndicator(symbol).orElseThrow());
        }
        return indiceIndicators;
    }

    public Optional<IndiceIndicator> getIndiceIndicator(IndiceId symbol) {
        // minute ohlcv
        LocalDateTime minuteMaxDateTime = indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndOhlcvType(symbol, OhlcvType.MINUTE)
                .orElse(LocalDateTime.now());
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndOhlcvType(symbol, OhlcvType.MINUTE, minuteMaxDateTime.minusDays(1), minuteMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyMaxDateTime = indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndOhlcvType(symbol, OhlcvType.DAILY)
                .orElse(LocalDateTime.now());
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndOhlcvType(symbol, OhlcvType.DAILY, dailyMaxDateTime.minusMonths(3), dailyMaxDateTime, Pageable.unpaged()).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // return indicator
        return Optional.of(IndiceIndicator.builder()
                .indiceId(symbol)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

}
