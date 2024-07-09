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

    public void save(TradeAsset profile) {
        // trim message
        profile.setMessage(Optional.ofNullable(profile.getMessage())
                .map(String::trim)
                .orElse(null));

        // send message
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSend(destination, jsonString);

        // persist entity
        if (this.persist) {
            TradeAssetEntity statusEntity = TradeAssetEntity.builder()
                    .tradeId(profile.getTradeId())
                    .assetId(profile.getAssetId())
                    .previousClose(profile.getPreviousClose())
                    .open(profile.getOpen())
                    .close(profile.getClose())
                    .message(profile.getMessage())
                    .build();
            profileRepository.save(statusEntity);
        }
    }

}
