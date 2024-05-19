package org.oopscraft.fintics.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsSummary implements Serializable {

    private String id;

    private String name;

    private Long totalCount;

    private LocalDateTime maxDateTime;

    private LocalDateTime minDateTime;

}
