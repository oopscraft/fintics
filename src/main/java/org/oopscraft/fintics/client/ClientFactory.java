package org.oopscraft.fintics.client;

import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Properties;

public class ClientFactory {

    public static Client getClient(String clientType, String clientProperties) {
        try {
            Class<? extends Client> clientTypeClass = Class.forName(clientType).asSubclass(Client.class);
            Constructor<? extends Client> constructor = clientTypeClass.getConstructor(Properties.class);
            Properties properties = loadPropertiesFromString(clientProperties);
            return constructor.newInstance(properties);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid client type: " + clientType, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + clientType, e);
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
