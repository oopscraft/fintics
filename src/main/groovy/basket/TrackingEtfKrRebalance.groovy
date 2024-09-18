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
    BigDecimal score
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
    def etfHoldings = getEtfHoldings(it)
    allEtfHoldings.addAll(etfHoldings)
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

    //  ROE
    def roes = asset.getAssetMetas('ROE').collect{new BigDecimal(it.value?:'0.0')}
    def roe = roes.find{true}?:0.0
    if (roe < 5.0) {    // ROE 5 이하는 수익성 없는 회사로 제외
        return false
    }

    // PER
    def pers = asset.getAssetMetas('PER').collect{new BigDecimal(it.value?:'100.0')}
    def per = pers.find{true}?:100.0

    // defines score
    it.score = roe/per as BigDecimal

    // return
    return it
}
println "finalEtfHoldings:${finalEtfHoldings}"

//=========================================
// sort by score
//=========================================
def targetAssetCount = 50
def targetHoldingWeightPerAsset = 2.0
def fixedAssetCount = basket.getBasketAssets().findAll{it.fixed && it.enabled}.size()
def remainedAssetCount = (targetAssetCount - fixedAssetCount) as Integer
finalEtfHoldings = finalEtfHoldings
        .sort{ -(it.score?:0)}
        .take(remainedAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceResult> basketRebalanceResults = finalEtfHoldings.collect{
    BasketRebalanceResult.of(it.symbol, it.name, targetHoldingWeightPerAsset)
}
println("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
