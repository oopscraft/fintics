package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetRepositoryCustom {

    Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable);

}
