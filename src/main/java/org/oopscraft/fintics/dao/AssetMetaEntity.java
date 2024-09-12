package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.common.data.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "fintics_asset_meta")
@IdClass(AssetMetaEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetMetaEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private String name;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "date_time")
    private Instant dateTime;

    @Column(name = "sort")
    private Integer sort;

}
