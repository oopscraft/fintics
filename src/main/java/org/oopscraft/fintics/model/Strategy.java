package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.StrategyEntity;

import javax.persistence.Converter;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Strategy {

    private String strategyId;

    private String strategyName;

    private Language language;

    private String script;

    public static enum Language {
        GROOVY
    }

    @Converter(autoApply = true)
    public static class LanguageConverter extends AbstractEnumConverter<Language> {}

    public static Strategy from(StrategyEntity ruleEntity) {
        return Strategy.builder()
                .strategyId(ruleEntity.getStrategyId())
                .strategyName(ruleEntity.getStrategyName())
                .language(ruleEntity.getLanguage())
                .script(ruleEntity.getScript())
                .build();
    }

}
