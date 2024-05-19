package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetNewsRepository extends JpaRepository<AssetNewsEntity, AssetNewsEntity.Pk> {

}
