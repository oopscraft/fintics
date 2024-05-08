package org.oopscraft.fintics.client.ohlcv.alphavantage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlphavantageOhlcvRepository extends JpaRepository<AlphavantageOhlcvEntity, AlphavantageOhlcvEntity.Pk> {

}
