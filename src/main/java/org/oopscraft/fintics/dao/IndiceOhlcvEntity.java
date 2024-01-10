package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.OhlcvType;

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

    public static class Pk implements Serializable {
        private IndiceId indiceId;
        private OhlcvType ohlcvType;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "indice_id", length = 32)
    @Enumerated(EnumType.STRING)    // @Id is converter not work
    private IndiceId indiceId;

}
