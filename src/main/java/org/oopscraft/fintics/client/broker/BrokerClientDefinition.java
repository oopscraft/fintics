package org.oopscraft.fintics.client.broker;

import org.oopscraft.fintics.model.Asset;
import org.springframework.beans.factory.Aware;

import java.util.List;

public interface BrokerClientDefinition extends Aware {

    String getBrokerId();

    String getBrokerName();

    List<Asset.Link> getAssetLinks(Asset asset);

    Class<? extends BrokerClient> getClassType();

    String getConfigTemplate();

}
