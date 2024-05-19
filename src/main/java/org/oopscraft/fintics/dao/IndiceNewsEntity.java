package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Indice;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_indice_news")
@IdClass(IndiceNewsEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndiceNewsEntity extends NewsEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private Indice.Id indiceId;
        private LocalDateTime dateTime;
        private String newsId;
    }

    @Id
    @Column(name = "indice_id", length = 32)
    @Enumerated(EnumType.STRING)       // @Id is converter not work
    private Indice.Id indiceId;

}
