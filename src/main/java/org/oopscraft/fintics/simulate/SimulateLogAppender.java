package org.oopscraft.fintics.simulate;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
public class SimulateLogAppender extends AppenderBase<ILoggingEvent> {

    private final Simulate simulate;

    private final PatternLayout layout;

    private final SimpMessagingTemplate messagingTemplate;

    public SimulateLogAppender(Simulate simulate, Context context, SimpMessagingTemplate messagingTemplate) {
        this.simulate = simulate;
        this.messagingTemplate = messagingTemplate;
        layout = new PatternLayout();
        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] - %msg");
        layout.setContext(context);
        layout.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String destination = String.format("/simulate/%s/log", simulate.getSimulateId());
        String logMessage = layout.doLayout(event);
        messagingTemplate.convertAndSend(destination, logMessage);
    }

}
