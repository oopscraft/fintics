package org.oopscraft.fintics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.market.MarketClient;
import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.model.MarketIndicator;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

    private static final String CACHE_MARKET = "MarketService.getMarket";

    private final MarketClient marketClient;

    @Cacheable(value = CACHE_MARKET)
    public synchronized Market getMarket() {
        return Market.builder()
                .ndxIndicator(marketClient.getNdxIndicator())
                .ndxFutureIndicator(marketClient.getNdxFutureIndicator())
                .spxIndicator(marketClient.getSpxIndicator())
                .spxFutureIndicator(marketClient.getSpxFutureIndicator())
                .djiIndicator(marketClient.getDjiIndicator())
                .djiFutureIndicator(marketClient.getDjiFutureIndicator())
                .build();
    }

    @CachePut(value = CACHE_MARKET)
    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public synchronized Market putMarketCache() {
        log.info("cacheEvict[{}]", CACHE_MARKET);
        return getMarket();
    }

}
