package org.oopscraft.fintics.api.v1.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketRequest {

    private String basketId;

    private String basketName;

    @Builder.Default
    private List<BasketAssetRequest> basketAssets = new ArrayList<>();

}
