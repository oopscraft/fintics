package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.BrokerAssetEntity;
import org.oopscraft.fintics.dao.TradeAssetEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAsset extends Asset {

    private String brokerId;

    private Type type;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    public enum Type {
        STOCK, ETF
    }

    @Converter(autoApply = true)
    public static class TypeConverter extends AbstractEnumConverter<Type> { }

    public static BrokerAsset from(BrokerAssetEntity brokerAssetEntity) {
        BrokerAsset brokerAsset = BrokerAsset.builder()
                .brokerId(brokerAssetEntity.getBrokerId())
                .assetId(brokerAssetEntity.getAssetId())
                .assetName(brokerAssetEntity.getAssetName())
                .dateTime(brokerAssetEntity.getDateTime())
                .type(brokerAssetEntity.getType())
                .marketCap(brokerAssetEntity.getMarketCap())
                .issuedShares(brokerAssetEntity.getIssuedShares())
                .per(brokerAssetEntity.getPer())
                .roe(brokerAssetEntity.getRoe())
                .roa(brokerAssetEntity.getRoa())
                .build();
        BrokerClientFactory.getBrokerClientDefinition(brokerAsset.getBrokerId()).ifPresent(brokerClientDefinition ->
                brokerAsset.setLinks(brokerClientDefinition.getAssetLinks(brokerAsset))
        );
        return brokerAsset;
    }

}
