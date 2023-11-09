package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrderEntity,TradeOrderEntity.Pk> {

}
