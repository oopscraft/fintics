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
        return Arrays.stream(Indice.Id.values())
                .map(Indice::from)
                .toList();
    }

    public Optional<Indice> getIndice(String indiceId) {
        return Optional.of(Indice.from(Indice.Id.valueOf(indiceId)));
    }

    public List<Ohlcv> getIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
    }

}
