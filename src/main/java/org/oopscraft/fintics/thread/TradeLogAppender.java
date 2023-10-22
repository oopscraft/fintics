package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.Layout;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class TradeLogAppender extends AppenderBase<ILoggingEvent> {

    private final PatternLayout layout;

    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public TradeLogAppender(Context context) {
        layout = new PatternLayout();
        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] - %msg");
        layout.setContext(context);
        layout.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            String logMessage = layout.doLayout(event);
            for(String logMessageLine : logMessage.split("\n")) {
                for(SseEmitter sseEmitter : sseEmitters) {
                    sseEmitter.send(logMessageLine);
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public synchronized void addSseEmitter(SseEmitter sseEmitter) {
        sseEmitters.add(sseEmitter);
    }

    public synchronized void removeSseEmitter(SseEmitter sseEmitter) {
        sseEmitters.remove(sseEmitter);
    }

}
