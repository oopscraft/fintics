package org.oopscraft.fintics.model.broker;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.Broker;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.*;

@Component
@RequiredArgsConstructor
public class BrokerClientFactory {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    public BrokerClient getObject(Broker broker) {
        BrokerClientDefinition brokerClientDefinition = brokerClientDefinitionRegistry.getBrokerClientDefinition(broker.getBrokerId()).orElseThrow();
        try {
            Class<? extends BrokerClient> clientClass = brokerClientDefinition.getClassType().asSubclass(BrokerClient.class);
            Constructor<? extends BrokerClient> constructor = clientClass.getConstructor(BrokerClientDefinition.class, Properties.class);
            Properties properties = loadPropertiesFromString(broker.getBrokerClientProperties());
            return constructor.newInstance(brokerClientDefinition, properties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
