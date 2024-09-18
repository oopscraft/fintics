import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.jsoup.Jsoup
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
    def document = Jsoup.connect("https://finance.yahoo.com/quote/${etfSymbol}/holdings/").get()
    def topHoldings= document.select('section[data-testid=top-holdings] .content')
    return topHoldings.collect {
        def symbol = it.select('span.symbol').text()
        def name = it.select('span.name').text()
        def weight = it.select('span.data').text().replace('%','') as BigDecimal
        return EtfHolding.builder()
            .symbol(symbol)
            .name(name)
            .weight(weight)
            .build()
    }
}

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
    Asset asset = assetService.getAsset("US.${it.symbol}").orElse(null)
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
