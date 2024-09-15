import org.oopscraft.fintics.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.*
import groovy.json.JsonSlurper
import org.oopscraft.fintics.basket.BasketChange

/**
 * holding
 */
class Holding {
    String symbol
    String name
    String weight
    @Override
    String toString() {
        return "Holding{symbol=${this.symbol},name=${this.name},weight=${this.weight}"
    }
    static Holding of(symbol, name, weight) {
        Holding holding = new Holding()
        holding.symbol = symbol
        holding.name = name
        holding.weight = weight
        return holding
    }
}

/**
 * gets ETF holdings
 * @param etfSymbol
 * @return
 */
static List<Holding> getHoldings(etfSymbol) {
    def url= new URL("https://m.stock.naver.com/api/stock/${etfSymbol}/etfAnalysis")
    def responseJson= url.text
    def jsonSlurper = new JsonSlurper()
    def responseMap = jsonSlurper.parseText(responseJson)
    def pdfAssets = responseMap.get('etfTop10MajorConstituentAssets')
    return pdfAssets.collect{
        Holding.of(it.itemCode, it.itemName, it.etfWeight.replace('%','') as BigDecimal)
    }
}

// ETF list
def etfSymbols = [
        // index ETF
        '385720',   // TIMEFOLIO 코스피액티브
        // strategy ETF
        '441800',   // TIMEFOLIO Korea플러스배당액티브
        '280920',   // PLUS 주도업종
        '373490',   // KODEX K-이노베이션액티브
        '410870',   // TIMEFOLIO K컬처액티브
        '161510',   // PLUS 고배당주
        // sector ETF
        '396500',   // TIGER Fn반도체TOP10
        '463050',   // TIMEFOLIO K바이오액티브
        '462900',   // KoAct 바이오헬스케어액티브
        '385510',   // KODEX K-신재생에너지액티브
        '228790',   // TIGER 화장품
        '449450',   // PLUS K방산
        '434730',   // HANARO 원자력iSelect
        // TODO 수익률 상위 ETF 추가
]

//=======================================
// collect etf holdings
//=======================================
List<Holding> allHoldings = []
etfSymbols.each{
    def cuAssets = getHoldings(it)
    allHoldings.addAll(cuAssets)
}
println "allHoldings:${allHoldings}"

//========================================
// distinct sum
//========================================
def distinctHoldings = allHoldings.groupBy{
    it.symbol
}.collect { symbol, holdingsList ->
    def name = holdingsList.first().name
    def totalWeight = holdingsList.sum { it.weight.toBigDecimal() }  // weight 값을 합산
    return Holding.of(symbol, name, totalWeight)
}.sort{
    - it.weight.toBigDecimal()
}
println "distinctHoldings:${distinctHoldings}"

//=========================================
// filter
//=========================================
List<Holding> finalHoldings = distinctHoldings.findAll {
    Asset asset = assetService.getAsset("KR.${it.symbol}").orElse(null)
    if (asset == null) {
        return null
    }
    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return null
    }
    return it
}

//=========================================
// return
//=========================================
List<BasketChange> basketChanges = finalHoldings.collect{
    BasketChange.of(it.symbol, it.name, 2.0)
}
println("basketChanges: ${basketChanges}")
return basketChanges
