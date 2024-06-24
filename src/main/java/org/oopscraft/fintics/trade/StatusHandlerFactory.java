package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.TradeAssetStatusRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatusHandlerFactory {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetStatusRepository tradeAssetStatusRepository;

    public StatusHandler getObject(String destination, boolean persist) {
        return StatusHandler.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .persist(persist)
                .tradeAssetStatusRepository(tradeAssetStatusRepository)
                .build();
    }

}
