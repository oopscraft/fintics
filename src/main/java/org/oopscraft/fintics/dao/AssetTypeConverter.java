package org.oopscraft.fintics.dao;

import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.arch4j.core.user.UserStatus;
import org.oopscraft.fintics.model.AssetType;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class AssetTypeConverter extends AbstractEnumConverter<AssetType> {

}
