package org.oopscraft.fintics.client.trade;

import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.Trade;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Properties;

public class TradeClientFactory {

    public static TradeClient getClient(String clientId, String clientConfig) {
        TradeClientDefinition tradeClientDefinition = TradeClientDefinitionRegistry.getTradeClientDefinition(clientId).orElseThrow();
        try {
            Class<? extends TradeClient> clientTypeClass = tradeClientDefinition.getType().asSubclass(TradeClient.class);
            Constructor<? extends TradeClient> constructor = clientTypeClass.getConstructor(Properties.class);
            Properties clientConfigProperties = loadPropertiesFromString(clientConfig);
            return constructor.newInstance(clientConfigProperties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + tradeClientDefinition.getType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TradeClient getClient(Trade trade) {
        return getClient(trade.getTradeClientId(), trade.getTradeClientConfig());
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
