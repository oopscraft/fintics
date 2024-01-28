package org.oopscraft.fintics.model;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.IndiceId;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class IndiceIdConverter extends AbstractEnumConverter<IndiceId> {

}
