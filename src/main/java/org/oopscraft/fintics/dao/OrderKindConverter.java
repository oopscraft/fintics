package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.OrderKind;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class OrderKindConverter extends AbstractEnumConverter<OrderKind> {

}
