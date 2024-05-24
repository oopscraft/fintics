package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Indice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IndiceNewsRepository extends JpaRepository<IndiceNewsEntity, IndiceNewsEntity.Pk> {

    @Query("select a from IndiceNewsEntity a" +
            " where a.indiceId = :indiceId" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<IndiceNewsEntity> findAllByIndiceId(
            @Param("indiceId") Indice.Id indiceId,
            @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
            @Param("dateTimeTo") LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
