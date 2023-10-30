import java.time.LocalDateTime;
import java.time.LocalTime;

int period = 10;
Boolean hold;
LocalDateTime dateTime = assetIndicator.getMinuteDateTimes().get(0);
double price = assetIndicator.getMinutePrices().get(0);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(20), period);
double macdOscillatorAverage = tool.average(assetIndicator.getMinuteMacds(24, 52, 18).collect { it.oscillator }, period);
double rsiAverage = tool.average(assetIndicator.getMinuteRsis(28), period);

double adxAverage = tool.average(assetIndicator.getMinuteDmis(28).collect { it.adx }, period);
double adxPdiAverage = tool.average(assetIndicator.getMinuteDmis(28).collect { it.pdi }, period);
double adxMdiAverage = tool.average(assetIndicator.getMinuteDmis(28).collect { it.mdi }, period);

log.info("- dateTime:{}, price:{}, emaSlope:{}, macdOscillatorAverage:{}, rsiAverage:{}, adxAverage:{}, adxPdiAverage:{}, adxMdiAverage:{}",
        dateTime, price, emaSlope, macdOscillatorAverage, rsiAverage, adxAverage, adxPdiAverage, adxMdiAverage);

// 매수조건
if(emaSlope > 0) {
    if(macdOscillatorAverage > 0
            && rsiAverage > 50
            && (adxAverage > 20 && adxPdiAverage > adxMdiAverage)
    ) {
        hold = true;
    }
}

// 매도조건
if(emaSlope < 0) {
    if(macdOscillatorAverage < 0
            || rsiAverage < 50
            || (adxAverage < 20 || adxMdiAverage > adxPdiAverage)
    ) {
        hold = false;
    }
}

// 장종료전 모두 청산(보유하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,15))) {
    hold = false;
}

// 결과 반환
return hold;
