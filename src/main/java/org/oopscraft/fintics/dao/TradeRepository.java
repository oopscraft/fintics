package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity,String> {

    @Query("select a from TradeEntity a" +
            " order by a.tradeName")
    List<TradeEntity> findAllOrderByName();

}
