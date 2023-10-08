package org.oopscraft.fintics.client;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ClientDefinitionRegistry implements BeanPostProcessor {

    @Getter
    private static final List<ClientDefinition> clientDefinitions = new ArrayList<>();

    public void add(ClientDefinition executorDefinition) {
        clientDefinitions.add(executorDefinition);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ClientDefinition) {
            clientDefinitions.add((ClientDefinition) bean);
        }
        return bean;
    }

    public static Optional<ClientDefinition> getProbeDefinition(String type) {
        return clientDefinitions.stream()
                .filter(probe -> Objects.equals(probe.getType().getName(), type))
                .findFirst();
    }

}
