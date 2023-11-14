package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.OrderResult;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class OrderResultConverter extends AbstractEnumConverter<OrderResult> {

}
