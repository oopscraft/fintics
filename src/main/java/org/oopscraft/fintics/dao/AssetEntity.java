package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AssetEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

}
