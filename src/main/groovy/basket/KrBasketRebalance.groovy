import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.oopscraft.fintics.basket.BasketRebalanceResult
import org.oopscraft.fintics.model.Asset

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    BigDecimal score
}

/**
 * gets fnguide items
 * @param url
 * @return
 * @see https://comp.fnguide.com/SVO/WooriRenewal/new_overview.asp
 */
static List<Item> getFnguideItems(String url) {
    def response = new URL(url).getText()
    if (response.charAt(0) == '\uFEFF') {
        response = response.substring(1)
    }
    def responseJson = new JsonSlurper().parseText(response)
    def compArray = responseJson.comp as List<Map>
    return compArray.collect {
        Item.builder()
                .symbol(it.GICODE.replace('A', '') as String)
                .name(it.ITEMABBRNM as String)
                .build()
    }
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
    def pdfAssets = responseMap.get('etfTop10MajorConstituentAssets')
    return pdfAssets.collect{
        Item.builder()
            .symbol(it.itemCode as String)
            .name(it.itemName as String)
            .build()
    }
}

//=======================================
// defines
//=======================================
List<Item> candidateItems = []

//=======================================
// FnGuide items
//=======================================
// FNGUID 성장성 - 고 ROE
def fnguidHighRoeItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/HIGH_ROE.json")
println("fnguidHighRoeItems: ${fnguidHighRoeItems}")
candidateItems.addAll(fnguidHighRoeItems)

// FNGUID 성장성 - 성장률
def fnguidGrowthSalesItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/GROWHT_SALES.json")
println("fnguidGrowthSalesItems: ${fnguidGrowthSalesItems}")
candidateItems.addAll(fnguidGrowthSalesItems)

// FNGUID 수급 - 외국인/기관 동반 순매수
def fnguidTrendWithBuyItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/SUPPLY_TREND_WITH_BUY.json")
println("fnguidTrendWithBuyItems: ${fnguidTrendWithBuyItems}")
candidateItems.addAll(fnguidTrendWithBuyItems)

// FNGUID 수급 - 연속순매수 (외국인/기관 동반)
def fnguidContinuousBuyWith3Items = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/CONTINUOUS_BUY_WITH_3.json")
println("fnguidContinuousBuyWith3Items: ${fnguidContinuousBuyWith3Items}")
candidateItems.addAll(fnguidContinuousBuyWith3Items)

//=======================================
// collect etf items
//=======================================
// ETF list
def etfSymbols = [
        // index ETF
        '069500',   // KODEX 200
        '229200',   // KODEX 코스닥150
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
    def roe = roes.find{true}?:0.0
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

    // defines score
    it.score = roe/per as BigDecimal

    // return
    return it
}
println "finalItems: ${finalItems}"

//=========================================
// sort by score
//=========================================
def targetAssetCount = 50
def targetHoldingWeightPerAsset = 2.0
def fixedAssetCount = basket.getBasketAssets().findAll{it.fixed && it.enabled}.size()
def remainedAssetCount = (targetAssetCount - fixedAssetCount) as Integer
finalItems = finalItems
        .sort{ -(it.score?:0)}
        .take(remainedAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceResult> basketRebalanceResults = finalItems.collect{
    BasketRebalanceResult.of(it.symbol, it.name, targetHoldingWeightPerAsset)
}
println("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
