package org.oopscraft.fintics.client.broker;

import lombok.Getter;
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
public class BrokerClientFactory implements BeanPostProcessor {

    @Getter
    private static final List<BrokerClientDefinition> brokerClientDefinitions = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof BrokerClientDefinition) {
            brokerClientDefinitions.add((BrokerClientDefinition) bean);
        }
        return bean;
    }

    public static Optional<BrokerClientDefinition> getBrokerClientDefinition(String brokerId) {
        return brokerClientDefinitions.stream()
                .filter(item -> Objects.equals(item.getBrokerId(), brokerId))
                .findFirst();
    }

    public BrokerClient getObject(String brokerId, String brokerConfig) {
        BrokerClientDefinition brokerClientDefinition = getBrokerClientDefinition(brokerId).orElseThrow();
        try {
            Class<? extends BrokerClient> clientTypeClass = brokerClientDefinition.getClassType().asSubclass(BrokerClient.class);
            Constructor<? extends BrokerClient> constructor = clientTypeClass.getConstructor(BrokerClientDefinition.class, Properties.class);
            Properties clientConfigProperties = loadPropertiesFromString(brokerConfig);
            return constructor.newInstance(brokerClientDefinition, clientConfigProperties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + brokerClientDefinition.getClassType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BrokerClient getObject(Trade trade) {
        return getObject(trade.getBrokerId(), trade.getBrokerConfig());
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
