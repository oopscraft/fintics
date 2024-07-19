package org.oopscraft.fintics.client.broker.kis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KisAccessThrottler {

    private static final ConcurrentHashMap<String, LockAndCondition> requestLocks = new ConcurrentHashMap<>();

    private static class LockAndCondition {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        long lastAccessTime = 0;
    }

    public static void sleep(String appKey, long sleepMillis) throws InterruptedException {
        LockAndCondition lockAndCondition = requestLocks.computeIfAbsent(appKey, k -> new LockAndCondition());
        lockAndCondition.lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastAccess = currentTime - lockAndCondition.lastAccessTime;

            if (timeSinceLastAccess < sleepMillis) {
                long sleepTime = sleepMillis - timeSinceLastAccess;
                lockAndCondition.condition.awaitNanos(sleepTime * 1_000_000);
            }

            // Update the last access time for this access token
            lockAndCondition.lastAccessTime = currentTime;
        } finally {
            lockAndCondition.lock.unlock();
        }
    }

}
