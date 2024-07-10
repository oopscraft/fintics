package org.oopscraft.fintics.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeAssetRepository;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

@Builder
@Getter
public class TradeAssetStore {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final String destination;

    private final boolean persist;

    private final TradeAssetRepository profileRepository;

    /**
     * saves trade asset
     * 1. sends stomp message to specific destination
     * 2. if persist flag is true, save to database table
     * @param tradeAsset trade asset to handle
     */
    public void save(TradeAsset tradeAsset) {
        // trim message
        tradeAsset.setMessage(Optional.ofNullable(tradeAsset.getMessage())
                .map(String::trim)
                .orElse(null));

        // send message
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(tradeAsset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSend(destination, jsonString);

        // persist entity
        if (this.persist) {
            TradeAssetEntity statusEntity = TradeAssetEntity.builder()
                    .tradeId(tradeAsset.getTradeId())
                    .assetId(tradeAsset.getAssetId())
                    .previousClose(tradeAsset.getPreviousClose())
                    .open(tradeAsset.getOpen())
                    .close(tradeAsset.getClose())
                    .message(tradeAsset.getMessage())
                    .build();
            profileRepository.save(statusEntity);
        }
    }

}
