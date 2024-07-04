package org.oopscraft.fintics.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, NewsEntity.Pk> {

    @Query("select a from NewsEntity a" +
            " where a.assetId = :assetId" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<NewsEntity> findAllByAssetId(
           @Param("assetId") String assetId,
           @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
           @Param("dateTimeTo") LocalDateTime dateTimeTo,
           Pageable pageable
    );

}
