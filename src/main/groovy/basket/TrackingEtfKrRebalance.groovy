import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.oopscraft.fintics.basket.BasketRebalanceResult
import org.oopscraft.fintics.model.Asset

/**
 * etf holding
 */
@Builder
@ToString
class EtfHolding {
    String symbol
    String name
    BigDecimal weight
    BigDecimal marketCap
}

/**
 * gets ETF holdings
 * @param etfSymbol
 * @return
 */
static List<EtfHolding> getEtfHoldings(etfSymbol) {
    def url= new URL("https://m.stock.naver.com/api/stock/${etfSymbol}/etfAnalysis")
    def responseJson= url.text
    def jsonSlurper = new JsonSlurper()
    def responseMap = jsonSlurper.parseText(responseJson)
    def pdfAssets = responseMap.get('etfTop10MajorConstituentAssets')
    return pdfAssets.collect{
        EtfHolding.builder()
            .symbol(it.itemCode as String)
            .name(it.itemName as String)
            .weight(it.etfWeight.replace('%','') as BigDecimal)
            .build()
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
List<EtfHolding> allEtfHoldings = []
etfSymbols.each{
    def cuAssets = getEtfHoldings(it)
    allEtfHoldings.addAll(cuAssets)
}
println "allEtfHoldings:${allEtfHoldings}"



//========================================
// distinct sum of weight
//========================================
def distinctEtfHoldings = allEtfHoldings.groupBy{
    it.symbol
}.collect { symbol, holdingsList ->
    def name = holdingsList.first().name
    def totalWeight = holdingsList.sum { it.weight.toBigDecimal() } as BigDecimal
    return EtfHolding.builder()
        .symbol(symbol)
        .name(name)
        .weight(totalWeight)
        .build()
}
println "distinctEtfHoldings:${distinctEtfHoldings}"

//=========================================
// filter
//=========================================
List<EtfHolding> finalEtfHoldings = distinctEtfHoldings.findAll {
    Asset asset = assetService.getAsset("KR.${it.symbol}").orElse(null)
    if (asset == null) {
        return false
    }
    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }
    // set marketCap
    it.marketCap = asset.getMarketCap()
    // checks ROE
    def roeMeta = asset.getAssetMetas().find { it.name == 'ROE' }
    def roe = (roeMeta != null && roeMeta.value != null) ? new BigDecimal(roeMeta.value) : BigDecimal.ZERO
    if (roe < 20) {
        return false
    }
    // return
    return it
}
println "finalEtfHoldings:${finalEtfHoldings}"

//=========================================
// sort by market cap
//=========================================
finalEtfHoldings = finalEtfHoldings
        .sort{-(it.marketCap?:0)}
        .take(40)

//=========================================
// return
//=========================================
List<BasketRebalanceResult> basketRebalanceResults = finalEtfHoldings.collect{
    BasketRebalanceResult.of(it.symbol, it.name, 2.0)
}
println("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
