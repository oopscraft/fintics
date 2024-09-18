import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.jsoup.Jsoup
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
 * gets ETF holdings
 * @param etfSymbol
 * @return
 */
static List<Item> getEtfItems(etfSymbol) {
    def document = Jsoup.connect("https://finance.yahoo.com/quote/${etfSymbol}/holdings/").get()
    def topHoldings= document.select('section[data-testid=top-holdings] .content')
    return topHoldings.collect {
        def symbol = it.select('span.symbol').text()
        def name = it.select('span.name').text()
        return Item.builder()
            .symbol(symbol)
            .name(name)
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
        'SPY',      // SPDR S&P 500 ETF Trust
        'QQQ',      // Invesco QQQ Trust
        'DIA',      // SPDR Dow Jones Industrial Average ETF Trust
        'VTI',      // Vanguard Total Stock Market Index Fund ETF Shares
        // strategy ETF
        'JEPI',     // JPMorgan Equity Premium Income ETF
        'JEPQ',     // JPMorgan Nasdaq Equity Premium Income ETF
        'DGRW',     // WisdomTree U.S. Quality Dividend Growth Fund
        'DIVO',     // Amplify CWP Enhanced Dividend Income ETF
        'MOAT',     // VanEck Morningstar Wide Moat ETF
        'SCHD',     // Schwab U.S. Dividend Equity ETF
        // sector ETF
        'XLK',      // The Technology Select Sector SPDR Fund
        'VGT',      // Vanguard Information Technology Index Fund ETF Shares
        'SMH',      // VanEck Semiconductor ETF
        'SOXX',     // iShares Semiconductor ETF
        'XLV',      // The Health Care Select Sector SPDR Fund
        'XBI',      // SPDR S&P Biotech ETF
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
    Asset asset = assetService.getAsset("US.${it.symbol}").orElse(null)
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
