package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageTemplateFactory {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    public MessageTemplate getObject(String destination) {
        return MessageTemplate.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .build();
    }

}
