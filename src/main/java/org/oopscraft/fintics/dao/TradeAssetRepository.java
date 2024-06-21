package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TradeAssetRepository extends JpaRepository<TradeAssetEntity, TradeAssetEntity.Pk> {

    @Modifying
    @Transactional
    @Query("update TradeAssetEntity a SET a.message = :message WHERE a.tradeId = :tradeId AND a.assetId = :assetId")
    void updateMessage(@Param("tradeId") String tradeId, @Param("assetId") String assetId, @Param("message") String message);

}
