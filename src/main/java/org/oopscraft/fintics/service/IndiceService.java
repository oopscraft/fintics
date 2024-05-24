package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.IndiceNewsRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndiceService {

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final IndiceNewsRepository indiceNewsRepository;

    public List<Indice> getIndices() {
        return Arrays.stream(Indice.Id.values())
                .map(Indice::from)
                .toList();
    }

    public Optional<Indice> getIndice(String indiceId) {
        return Optional.of(Indice.from(Indice.Id.valueOf(indiceId)));
    }

    public List<Ohlcv> getIndiceDailyOhlcvs(Indice.Id indiceId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
    }

    public List<Ohlcv> getIndiceMinuteOhlcvs(Indice.Id indiceId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
    }

    public List<News> getIndiceNewses(Indice.Id indiceId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return indiceNewsRepository.findAllByIndiceId(indiceId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(News::from)
                .toList();
    }


}
