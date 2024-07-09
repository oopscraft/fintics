package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.TradeAssetRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeAssetStoreFactory {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetRepository profileRepository;

    public TradeAssetStore getObject(String destination, boolean persist) {
        return TradeAssetStore.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .persist(persist)
                .profileRepository(profileRepository)
                .build();
    }

}
