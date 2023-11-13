package org.oopscraft.fintics.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.IndiceIndicatorResponse;
import org.oopscraft.fintics.api.v1.dto.IndiceResponse;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/indice")
@RequiredArgsConstructor
public class IndiceRestController {

    private final IndiceService indiceService;

    @RequestMapping
    public ResponseEntity<List<IndiceResponse>> getIndices() {
        List<IndiceResponse> indiceResponses = indiceService.getIndices().stream()
                .map(IndiceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indiceResponses);
    }

    @RequestMapping("{symbol}")
    public ResponseEntity<IndiceResponse> getIndice(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceResponse indiceResponse = indiceService.getIndice(symbol)
                .map(IndiceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceResponse);
    }

    @RequestMapping("{symbol}/indicator")
    public ResponseEntity<IndiceIndicatorResponse> getIndiceIndicator(@PathVariable("symbol")IndiceSymbol symbol) {
        IndiceIndicatorResponse indiceIndicatorResponse = indiceService.getIndiceIndicator(symbol)
                .map(IndiceIndicatorResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(indiceIndicatorResponse);
    }

}
