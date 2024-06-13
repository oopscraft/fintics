package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Builder
public class MessageTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final String destination;

    private TradeAsset tradeAsset;

    public void send(String message) {
        Map<String, String> messageMap = new LinkedHashMap<>();
        messageMap.put("tradeId", tradeAsset.getTradeId());
        messageMap.put("assetId", tradeAsset.getAssetId());
        messageMap.put("message", Optional.ofNullable(message).map(String::trim).orElse(null));
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(messageMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSend(destination, messageJson);
    }

    public MessageTemplate clone(TradeAsset tradeAsset) {
        return MessageTemplate.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .tradeAsset(tradeAsset)
                .build();
    }

}
