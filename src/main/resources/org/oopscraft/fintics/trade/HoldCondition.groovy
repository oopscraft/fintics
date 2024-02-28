import org.oopscraft.fintics.calculator.Bb
import org.oopscraft.fintics.calculator.BbContext
import org.oopscraft.fintics.model.Ohlcv

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
def ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def ohlcv = ohlcvs.first()
def closePrices = ohlcvs.collect{it.closePrice}
def closePrice = closePrices.first()
log.info("[{}] closePrice: {}", assetName, closePrice)

// bb
List<Bb> bbs = tool.calculate(ohlcvs, BbContext.DEFAULT)
def bb = bbs.first()
def bbMbbs = bbs.collect{it.mbb}
def bbMbb = bbMbbs.first()
def bbMbbPctChange = tool.pctChange(bbs.collect{it.mbb}.take(10))
def bbUbbs = bbs.collect{it.ubb}
def bbUbb = bbUbbs.first()
def bbLbbs = bbs.collect{it.lbb}
def bbLbb = bbLbbs.first()
log.info("[{}] Bb: {}", assetName, bb)
log.info("[{}] bbMbbPctChange: {}", assetName, bbMbbPctChange)

//==============================
// sideways within band
//==============================
// goes up
if (bbMbbPctChange > 0) {
    // middle band
    if (closePrices.get(1) < bbMbbs.get(1) && closePrice ) {
        hold = 1
    }
    // upper band
    if (closePrices.get(1) > bbUbbs.get(1)) {
        hold = 0
    }
}
// goes down
if (bbMbbPctChange < 0) {
    // lower band
    if (closePrice < bbLbb) {
        hold = 1
    }
    // middle band
    if (ohlcv.closePrice > bbMbb) {
        hold = 0
    }
}

//==============================
// break out band (fallback)
//==============================
// upper band
if (closePrices.take(2).every{it > bbUbb}) {
    log.info("###################### == [{}] closePrices: {}", assetName, closePrices.take(3))
    log.info("###################### == [{}] bbUbb: {}", assetName, bbUbb)
    hold = 1
}
// lower band
if (closePrices.take(2).every{it < bbLbb}) {
    log.info("###################### == [{}] closePrices: {}", assetName, closePrices.take(3))
    log.info("###################### == [{}] bbLbb: {}", assetName, bbLbb)
    hold = 0
}




// return
return hold

