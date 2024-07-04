package org.oopscraft.fintics.client.financial;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Financial;

public abstract class FinancialClient {

    @Getter
    private final FinancialClientProperties financialClientProperties;

    protected FinancialClient(FinancialClientProperties financialClientProperties) {
        this.financialClientProperties = financialClientProperties;
    }

    public abstract Financial getAssetFinancial(Asset asset);

}
