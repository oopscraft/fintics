package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.AssetType;
import org.oopscraft.fintics.model.IndiceSymbol;

import javax.persistence.Converter;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

@Converter(autoApply = true)
public class IndiceSymbolConverter extends AbstractEnumConverter<IndiceSymbol> {

}