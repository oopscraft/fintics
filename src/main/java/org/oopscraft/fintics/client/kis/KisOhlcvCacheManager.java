package org.oopscraft.fintics.client.kis;

import org.oopscraft.fintics.model.Ohlcv;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KisOhlcvCacheManager {

    public final Map<String,KisOhlcvCache> minuteOhlcvCacheMap = new ConcurrentHashMap<>();

    public synchronized List<Ohlcv> getMinuteOhlcvCache(String assetId) {
        if (!minuteOhlcvCacheMap.containsKey(assetId)) {
            minuteOhlcvCacheMap.put(assetId, new KisOhlcvCache());
        }
        return minuteOhlcvCacheMap.get(assetId).getCache();
    }

}
