import java.time.LocalTime;

int period = 10;
Boolean hold;
def time = assetIndicator.getMinuteDateTimes().get(0).toLocalTime();
def price = assetIndicator.getMinutePrices().get(0);

// MA indicator
def priceEma = assetIndicator.getMinuteEma(60);
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def priceSma = assetIndicator.getMinuteSma(20);
def priceSmaSlope = tool.slope(assetIndicator.getMinuteSmas(60), period);

// MACD indicator
def macds = assetIndicator.getMinuteMacds(60, 120, 30);
def macdOscillatorSlope = tool.slope(macds.collect { it.oscillator }, period);
def macdOscillatorAverage = tool.average(macds.collect { it.oscillator }, period);

// RSI indicator
def rsis = assetIndicator.getMinuteRsis(60);
def rsiSlope = tool.slope(rsis, period);
def rsiAverage = tool.average(rsis, period);

// DMI indicator
def dmis = assetIndicator.getMinuteDmis(60);
def dmiPdiSlope = tool.slope(dmis.collect { it.pdi }, period);
def dmiPdiAverage = tool.average(dmis.collect { it.pdi }, period);
def dmiMdiSlope = tool.slope(dmis.collect { it.mdi }, period);
def dmiMdiAverage = tool.average(dmis.collect { it.mdi }, period);
def dmiAdxSlope = tool.slope(dmis.collect { it.adx }, period);
def dmiAdxAverage = tool.average(dmis.collect { it.adx }, period);

log.info(
        "dateTime:{}, price:{}, priceEma:{}, priceEmaSlope:{}, priceSma:{}, priceSmaSlope:{} " +
        "macdOscillatorSlope:{}, macdOscillatorAverage:{}, " +
        "rsiSlope:{}, rsiAverage:{}, " +
        "dmiPdiSlope:{}, dmiPdiAverage:{}, dmiMdiSlope:{}, dmiMdiAverage:{}, dmiAdxSlope:{}, dmiAdxAverage:{}",
        dateTime, price, priceEma, priceEmaSlope, priceSma, priceSmaSlope,
        macdOscillatorSlope, macdOscillatorAverage,
        rsiSlope, rsiAverage,
        dmiPdiSlope, dmiPdiAverage, dmiMdiSlope, dmiMdiAverage, dmiAdxSlope, dmiAdxAverage
);

// 매수조건
if(priceEmaSlope > 0) {
    if(1 == 1
            && macdOscillatorAverage > 0
            && rsiAverage > 50
            && dmiPdiAverage > dmiMdiAverage
    ){
        hold = true;
    }
}

// 매도조건
if(priceEmaSlope < 0) {
    if(1 == 1
            && macdOscillatorAverage < 0
            && rsiAverage < 50
            && dmiPdiAverage < dmiMdiAverage
    ) {
        hold = false;
    }
}

// 장종료전 모두 청산(보유하지 않음)
if(time.isAfter(LocalTime.of(15,15))) {
    hold = false;
}

// 결과 반환
return hold;
