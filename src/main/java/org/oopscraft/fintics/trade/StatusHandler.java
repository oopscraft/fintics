package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.TradeAssetStatusEntity;
import org.oopscraft.fintics.dao.TradeAssetStatusRepository;
import org.oopscraft.fintics.model.TradeAssetStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

@Builder
@Getter
public class StatusHandler {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final String destination;

    private final boolean persist;

    private final TradeAssetStatusRepository tradeAssetStatusRepository;

    public void apply(TradeAssetStatus tradeAssetStatus) {
        // trim message
        tradeAssetStatus.setMessage(Optional.ofNullable(tradeAssetStatus.getMessage())
                .map(String::trim)
                .orElse(null));

        // send message
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(tradeAssetStatus);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSend(destination, jsonString);

        // persist entity
        if (this.persist) {
            TradeAssetStatusEntity tradeAssetStatusEntity = TradeAssetStatusEntity.builder()
                    .tradeId(tradeAssetStatus.getTradeId())
                    .assetId(tradeAssetStatus.getAssetId())
                    .previousClose(tradeAssetStatus.getPreviousClose())
                    .open(tradeAssetStatus.getOpen())
                    .close(tradeAssetStatus.getClose())
                    .message(tradeAssetStatus.getMessage())
                    .build();
            tradeAssetStatusRepository.save(tradeAssetStatusEntity);
        }
    }

}
