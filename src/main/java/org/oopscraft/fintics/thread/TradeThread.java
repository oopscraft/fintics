package org.oopscraft.fintics.thread;

import lombok.Getter;

public class TradeThread extends Thread {

    @Getter
    private final TradeRunnable tradeRunnable;

    public TradeThread(ThreadGroup threadGroup, TradeRunnable tradeRunnable, String name) {
        super(threadGroup, tradeRunnable, name);
        this.tradeRunnable = tradeRunnable;
    }

}
