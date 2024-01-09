package org.oopscraft.fintics.client.trade;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TradeClientDefinitionRegistry implements BeanPostProcessor {

    @Getter
    private static final List<TradeClientDefinition> tradeClientDefinitions = new ArrayList<>();

    public void add(TradeClientDefinition tradeClientDefinition) {
        tradeClientDefinitions.add(tradeClientDefinition);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof TradeClientDefinition) {
            tradeClientDefinitions.add((TradeClientDefinition) bean);
        }
        return bean;
    }

    public static Optional<TradeClientDefinition> getTradeClientDefinition(String clientId) {
        return tradeClientDefinitions.stream()
                .filter(clientDefinition -> Objects.equals(clientDefinition.getClientId(), clientId))
                .findFirst();
    }

}
