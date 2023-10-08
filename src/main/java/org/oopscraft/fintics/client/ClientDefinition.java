package org.oopscraft.fintics.client;

import org.springframework.beans.factory.Aware;

import java.util.Properties;

public interface ClientDefinition extends Aware {

    Class<? extends Client> getType();

    String getName();

    String getPropertiesTemplate();

}
