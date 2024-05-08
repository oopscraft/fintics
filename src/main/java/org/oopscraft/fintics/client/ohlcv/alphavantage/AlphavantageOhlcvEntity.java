package org.oopscraft.fintics.client.ohlcv.alphavantage;

import lombok.Builder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_alphavantage_ohlcv")
@IdClass(AlphavantageOhlcvEntity.Pk.class)
@Builder
public class AlphavantageOhlcvEntity {

    public static class Pk implements Serializable {
        private String symbol;
        private String interval;
        private LocalDateTime timestamp;
    }

    @Id
    @Column(name = "symbol", length = 16)
    private String symbol;

    @Id
    @Column(name = "interval", length = 8)
    private String interval;

    @Id
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "open")
    private BigDecimal open;

    @Column(name = "high")
    private BigDecimal high;

    @Column(name = "low")
    private BigDecimal low;

    @Column(name = "close")
    private BigDecimal close;

    @Column(name = "volume")
    private BigDecimal volume;

}
