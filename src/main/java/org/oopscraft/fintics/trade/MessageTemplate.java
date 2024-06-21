package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Setter;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Builder
public class MessageTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final String destination;

    private final Consumer<Message> onSend;

    private TradeAsset tradeAsset;

    public void send(String messageBody) {
        Message message = Message.builder()
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .body(Optional.ofNullable(messageBody).map(String::trim).orElse(null))
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSend(destination, messageJson);
        if (onSend != null) {
            onSend.accept(message);
        }
    }

    public MessageTemplate clone(TradeAsset tradeAsset) {
        return MessageTemplate.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .onSend(onSend)
                .tradeAsset(tradeAsset)
                .build();
    }

}
