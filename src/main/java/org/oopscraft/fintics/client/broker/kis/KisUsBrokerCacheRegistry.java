package org.oopscraft.fintics.client.broker.kis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class KisUsBrokerCacheRegistry {

    @Getter
    private final static Cache<PeriodOrderedSymbolsCacheKey, Optional<Set<String>>> periodOrderedSymbolsCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Getter
    private final static Cache<PeriodRightsCacheKey, Optional<List<Map<String,String>>>> periodRightsCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Getter
    private final static Cache<PaymentBalanceAssetCacheKey, Optional<Map<String, String>>> paymentBalanceAssetCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class PeriodOrderedSymbolsCacheKey {
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

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class PaymentBalanceAssetCacheKey {
        private String accountNo;
        private LocalDate date;
        private String symbol;
    }

}
