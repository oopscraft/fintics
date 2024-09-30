import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.oopscraft.fintics.basket.BasketRebalanceAsset
import org.oopscraft.fintics.model.Asset

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    String etfSymbol
    BigDecimal score
}

/**
 * gets etf items
 * @param etfSymbol
 * @return
 */
static List<Item> getEtfItems(etfSymbol) {
    def url= new URL("https://m.stock.naver.com/api/stock/${etfSymbol}/etfAnalysis")
    def responseJson= url.text
    def jsonSlurper = new JsonSlurper()
    def responseMap = jsonSlurper.parseText(responseJson)
    def top10Holdings = responseMap.get('etfTop10MajorConstituentAssets')
    // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
    if (top10Holdings == null || top10Holdings.size() < 10) {
        throw new NoSuchElementException("Top 10 holdings data is incomplete or missing - ${etfSymbol}")
    }
    return top10Holdings.collect{
        Item.builder()
                .symbol(it.itemCode as String)
                .name(it.itemName as String)
                .etfSymbol(etfSymbol)
                .build()
    }
}

//=======================================
// defines
//=======================================
List<Item> candidateItems = []

//=======================================
// collect etf items
//=======================================
// ETF list
def etfSymbols = [
        // index ETF
        '069500',   // KODEX 200
        '229200',   // KODEX 코스닥150
        '310970',   // TIGER MSCI Korea TR
        // strategy ETF
        '385720',   // TIMEFOLIO 코스피액티브
        '441800',   // TIMEFOLIO Korea플러스배당액티브
        '161510',   // PLUS 고배당주
        '104530',   // KOSEF 고배당
        '211560',   // TIGER 배당성장
        '139280',   // TIGER 경기방어
        '417630',   // TIGER KEDI혁신기업ESG30
        '280920',   // PLUS 주도업종
        '147970',   // TIGER 모멘텀
        '373490',   // KODEX K-이노베이션액티브
        '410870',   // TIMEFOLIO K컬처액티브
        // sector ETF
        '396500',   // TIGER Fn반도체TOP10
        '228790',   // TIGER 화장품
        '463050',   // TIMEFOLIO K바이오액티브
        '462900',   // KoAct 바이오헬스케어액티브
        '449450',   // PLUS K방산
        '139230',   // TIGER 200 중공업
        '434730',   // HANARO 원자력iSelect
]
etfSymbols.each{
    def etfItems = getEtfItems(it)
    println ("etfItems[${it}]: ${etfItems}")
    candidateItems.addAll(etfItems)
}

//========================================
// distinct items
//========================================
candidateItems = candidateItems.unique{it.symbol}
println "candidateItems: ${candidateItems}"

//=========================================
// filter
//=========================================
List<Item> finalItems = candidateItems.findAll {
    Asset asset = assetService.getAsset("KR.${it.symbol}").orElse(null)
    if (asset == null) {
        return false
    }

    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }

    //  ROE
    def roes = asset.getAssetMetas('ROE').collect{new BigDecimal(it.value?:'0.0')}
    def roe = roes.find{true}?:0.0 as BigDecimal
    if (roe < 5.0) {    // ROE 5 이하는 수익성 없는 회사로 제외
        return false
    }

    // ROA
    def roas = asset.getAssetMetas('ROA').collect{new BigDecimal(it.value?:'0.0')}
    def roa = roas.find{true}?:0.0
    if (roa < 0.0) {    // ROA 0 이하는 부채 비율이 높은 경우 일수 있음 으로 제외
        return false
    }

    // PER
    def pers = asset.getAssetMetas('PER').collect{new BigDecimal(it.value?:'100.0')}
    def per = pers.find{true}?:100.0

    // dividendYield
    def dividendYields = asset.getAssetMetas("Dividend Yield").collect{ new BigDecimal(it.value?:'0.0')}
    def dividendYield = dividendYields.find{true}?:0.0

    // score
    it.score = roe + dividendYield

    // return
    return it
}
println "finalItems: ${finalItems}"

//=========================================
// sort by score
//=========================================
def maxAssetCount = 50
def holdingWeightPerAsset = 2.0
def fixedAssetCount = basket.getBasketAssets().findAll{it.enabled && it.fixed}.size()
def targetAssetCount = (maxAssetCount - fixedAssetCount) as Integer
finalItems = finalItems
        .sort{ -(it.score?:0)}
        .take(targetAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceResults = finalItems.collect{
    BasketRebalanceAsset.of(it.symbol, it.name, holdingWeightPerAsset, null)
}
println("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
