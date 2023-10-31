package org.oopscraft.fintics.client.kis;

import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KisOhlcvCache {

    private Map<LocalDateTime, Ohlcv> cacheMap = new ConcurrentHashMap<>();

    public void putCache(List<Ohlcv> ohlcvs) {
        ohlcvs.forEach(ohlcv -> {
            cacheMap.put(ohlcv.getDateTime(), ohlcv);
        });
    }

    public List<Ohlcv> getCache() {
        return cacheMap.values().stream()
                .toList();
    }

}
