package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_indice_ohlcv")
@IdClass(IndiceOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndiceOhlcvEntity extends OhlcvEntity {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Pk implements Serializable {
        private Indice.Id indiceId;
        private Ohlcv.Type type;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "indice_id", length = 32)
    @Enumerated(EnumType.STRING)       // @Id is converter not work
    private Indice.Id indiceId;

}
