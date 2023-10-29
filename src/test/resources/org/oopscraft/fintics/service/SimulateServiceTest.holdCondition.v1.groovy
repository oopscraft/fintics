import java.time.LocalDateTime
import java.time.LocalTime

Boolean hold;
LocalDateTime dateTime = assetIndicator.getMinuteDateTimes().get(0);
double price = assetIndicator.getMinutePrices().get(0);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(20), 20);
double macdOscillatorAverage = tool.average(assetIndicator.getMinuteMacds(24, 52, 18).collect { it.oscillator }, 20);
double rsiAverage = tool.average(assetIndicator.getMinuteRsis(28), 20);

log.info("- dateTime:{}, price:{}, emaSlope:{}, macdOscillatorAverage:{}, rsiAverage:{}",
dateTime, price, emaSlope, macdOscillatorAverage, rsiAverage);

// 매수조건
if(emaSlope > 0) {
    if(macdOscillatorAverage > 0 && rsiAverage > 50) {
        hold = true;
    }
}

// 매도조건
if(emaSlope < 0) {
    if(macdOscillatorAverage < 0 && rsiAverage < 50) {
        hold = false;
    }
}

// 장종료전 모두 청산(보유하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,15))) {
    hold = false;
}

// 결과 반환
return hold;
