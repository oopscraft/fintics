package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.OhlcvType;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class OhlcvTypeConverter extends AbstractEnumConverter<OhlcvType> {

}
