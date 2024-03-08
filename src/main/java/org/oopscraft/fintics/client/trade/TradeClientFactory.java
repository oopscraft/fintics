package org.oopscraft.fintics.client.trade;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.Trade;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.*;

@Component
public class TradeClientFactory implements BeanPostProcessor {

    @Getter
    private static final List<TradeClientDefinition> tradeClientDefinitions = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if(bean instanceof TradeClientDefinition) {
            tradeClientDefinitions.add((TradeClientDefinition) bean);
        }
        return bean;
    }

    public static Optional<TradeClientDefinition> getTradeClientDefinition(String brokerId) {
        return tradeClientDefinitions.stream()
                .filter(item -> Objects.equals(item.getTradeClientId(), brokerId))
                .findFirst();
    }

    public TradeClient getObject(String brokerId, String brokerConfig) {
        TradeClientDefinition brokerClientDefinition = getTradeClientDefinition(brokerId).orElseThrow();
        try {
            Class<? extends TradeClient> clientTypeClass = brokerClientDefinition.getClassType().asSubclass(TradeClient.class);
            Constructor<? extends TradeClient> constructor = clientTypeClass.getConstructor(TradeClientDefinition.class, Properties.class);
            Properties clientConfigProperties = loadPropertiesFromString(brokerConfig);
            return constructor.newInstance(brokerClientDefinition, clientConfigProperties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + brokerClientDefinition.getClassType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TradeClient getObject(Trade trade) {
        return getObject(trade.getTradeClientId(), trade.getTradeClientConfig());
    }

    private static Properties loadPropertiesFromString(String propertiesString) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(propertiesString));
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid properties string", e);
        }

        properties = PbePropertiesUtil.decode(properties);
        properties = PbePropertiesUtil.unwrapDecryptedMark(properties);

        return properties;
    }

}
