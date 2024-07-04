package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetFinancial;

public abstract class AssetFinancialClient {

    @Getter
    private final AssetFinancialClientProperties financialClientProperties;

    protected AssetFinancialClient(AssetFinancialClientProperties financialClientProperties) {
        this.financialClientProperties = financialClientProperties;
    }

    public abstract AssetFinancial getAssetFinancial(Asset asset);

}
