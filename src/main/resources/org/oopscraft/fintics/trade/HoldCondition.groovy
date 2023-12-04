package org.oopscraft.fintics.trade

import java.time.LocalTime
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

// info
def symbol = indicator.getSymbol()
def name = indicator.getName()
def ohlcvType = OhlcvType.MINUTE
def period = 10

// rsi
def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
def rsi = rsis.first()
def rsiValues = rsis.collect{it.value}
def rsiValue = rsiValues.first()
def rsiSignals = rsis.collect{it.signal}
def rsiSignal = rsiSignals.first()
log.debug("[{}] rsi: {}", name, rsi)

// co
def cos = indicator.calculate(ohlcvType, period, CoContext.DEFAULT)
def co = cos.first()
def coValues = cos.collect{it.value}
def coValue = coValues.first()
def coSignals = cos.collect{it.signal}
def coSignal = coSignals.first()
log.debug("[{}] co: {}", name, co)

// hold
def hold = null;

// buy
if(rsiValue < 30 && rsiValue > rsiSignal) {
    if(coValue > 0 && coValue > coSignal) {
        hold = true
    }
}

// sell
if(rsiValue > 70 && rsiValue < rsiSignal) {
    if(coValue < 0 && coValue < coSignal) {
        hold = false
    }
}

// TODO loss cut
if(balance.hasBalanceAsset(indicator.getSymbol())) {
    def balanceAsset = balance.getBalanceAsset(indicator.getSymbol());
    log.info("########### balanceAsset:{}", balanceAsset);
}

// 장종료 전 매도 (보유 하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15, 15))) {
    hold = false
}

// return
return hold;