package org.oopscraft.fintics.client.broker.kis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class KisBrokerCacheRegistry {

    @Getter
    private final static Cache<PeriodOrdersCacheKey, Optional<List<Map<String, String>>>> periodOrdersCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Getter
    private final static Cache<PeriodRightsCacheKey, Optional<List<Map<String,String>>>> periodRightsCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class PeriodOrdersCacheKey {
        private String accountNo;
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class PeriodRightsCacheKey {
        private String symbol;
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }

}
