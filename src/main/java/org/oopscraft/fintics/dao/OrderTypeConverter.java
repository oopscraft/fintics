package org.oopscraft.fintics.dao;

import com.querydsl.core.types.Order;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.AssetType;
import org.oopscraft.fintics.model.OrderType;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class OrderTypeConverter extends AbstractEnumConverter<OrderType> {

}
