package org.oopscraft.fintics.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class AssetProfileTest {

    @Test
    void getOhlcvs() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(now.minusMinutes(1), 1000, 1100, 900, 1050, 100));
            add(Ohlcv.of(now.minusMinutes(2), 1060, 1200, 1030, 1040, 200));
            add(Ohlcv.of(now.minusMinutes(3), 1050, 1400, 800, 900, 300));
        }};

        // when
        AssetProfile assetProfile = AssetProfile.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertTrue(resampleOhlcvs.size() == 1);
        Ohlcv ohlcv = resampleOhlcvs.get(0);
        assertTrue(ohlcv.getOpenPrice().longValue() == 1050);
        assertTrue(ohlcv.getHighPrice().longValue() == 1400);
        assertTrue(ohlcv.getLowPrice().longValue() == 800);
        assertTrue(ohlcv.getClosePrice().longValue() == 1050);
        assertTrue(ohlcv.getVolume().longValue() == 600);
    }

    @Test
    void getOhlcvsWithUnderPeriod() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(now.minusMinutes(1), 1000, 1100, 900, 1050, 100));
            add(Ohlcv.of(now.minusMinutes(2), 1060, 1200, 1030, 1040, 200));
        }};

        // when
        AssetProfile assetProfile = AssetProfile.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertTrue(resampleOhlcvs.size() == 1);
        Ohlcv ohlcv = resampleOhlcvs.get(0);
        assertTrue(ohlcv.getOpenPrice().longValue() == 1060);
        assertTrue(ohlcv.getHighPrice().longValue() == 1200);
        assertTrue(ohlcv.getLowPrice().longValue() == 900);
        assertTrue(ohlcv.getClosePrice().longValue() == 1050);
        assertTrue(ohlcv.getVolume().longValue() == 300);
    }

    @Test
    void getOhlcvWithOverPeriod() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(now.minusMinutes(1), 1000, 1100, 900, 1050, 100));
            add(Ohlcv.of(now.minusMinutes(2), 1060, 1200, 1030, 1040, 200));
            add(Ohlcv.of(now.minusMinutes(3), 1050, 1400, 800, 900, 300));
            add(Ohlcv.of(now.minusMinutes(4), 1010, 1300, 1000, 1050, 200));
            add(Ohlcv.of(now.minusMinutes(5), 990, 1100, 980, 1000, 100));
        }};

        // when
        AssetProfile assetProfile = AssetProfile.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertTrue(resampleOhlcvs.size() == 2);
        Ohlcv ohlcv1 = resampleOhlcvs.get(0);
        assertTrue(ohlcv1.getOpenPrice().longValue() == 1050);
        assertTrue(ohlcv1.getHighPrice().longValue() == 1400);
        assertTrue(ohlcv1.getLowPrice().longValue() == 800);
        assertTrue(ohlcv1.getClosePrice().longValue() == 1050);
        assertTrue(ohlcv1.getVolume().longValue() == 600);
        Ohlcv ohlcv2 = resampleOhlcvs.get(1);
        assertTrue(ohlcv2.getOpenPrice().longValue() == 990);
        assertTrue(ohlcv2.getHighPrice().longValue() == 1300);
        assertTrue(ohlcv2.getLowPrice().longValue() == 980);
        assertTrue(ohlcv2.getClosePrice().longValue() == 1050);
        assertTrue(ohlcv2.getVolume().longValue() == 300);
    }

}