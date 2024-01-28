package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    public Optional<IndiceIndicator> getIndiceIndicator(IndiceId indiceId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        // minute ohlcv
        LocalDateTime minuteDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE)
                        .orElse(LocalDateTime.now()));
        LocalDateTime minuteDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(minuteDateTimeTo.minusDays(1));
        List<Ohlcv> minuteOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE, minuteDateTimeFrom, minuteDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(indiceOhlcvRepository.findMaxDateTimeByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY)
                        .orElse(LocalDateTime.now()));
        LocalDateTime dailyDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(dailyDateTimeTo.minusMonths(1));
        List<Ohlcv> dailyOhlcvs = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY, dailyDateTimeFrom, dailyDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // return indicator
        return Optional.of(IndiceIndicator.builder()
                .indiceId(indiceId)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

}
