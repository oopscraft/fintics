package org.oopscraft.fintics.trade.old

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

def ohlcvType = Ohlcv.Type.MINUTE
def ohlcvPeriod = 1
def pctChangePeriod = 3

// co
def cos = assetIndicator.calculate(ohlcvType, ohlcvPeriod, CoContext.DEFAULT)
def co = cos.first()
def coValues = cos.collect{it.value}
def coValue = coValues.first()
def coValuePctChange = tool.pctChange(coValues.take(pctChangePeriod))
def coSignals = cos.collect{it.signal}
def coSignal = coSignals.first()
log.debug("co: {}", co)


def hold = null
if(coValue > coSignal) {
    hold = true
}
if(coValue < coSignal) {
    hold = false;
}

return hold
