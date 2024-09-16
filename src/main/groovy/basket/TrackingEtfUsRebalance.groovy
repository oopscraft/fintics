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
}

/**
 * gets holdings
 * @param etfSymbol etf symbol
 * @return list of holding
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
        'VTI',      // Vanguard Total Stock Market Index Fund ETF Shares
        'SPY',      // SPDR S&P 500 ETF Trust
        'QQQ',      // Invesco QQQ Trust
        // strategy ETF
        'JEPI',     // JPMorgan Equity Premium Income ETF
        'JEPQ',     // JPMorgan Nasdaq Equity Premium Income ETF
        'DGRW',     // WisdomTree U.S. Quality Dividend Growth Fund
        'DIVO',     // Amplify CWP Enhanced Dividend Income ETF
        'MOAT',     // VanEck Morningstar Wide Moat ETF
        'SCHD',     // Schwab U.S. Dividend Equity ETF
        // sector ETF
        'XLK',      // The Technology Select Sector SPDR Fund
        'XLV',      // The Health Care Select Sector SPDR Fund
        // TODO 수익률 상위 ETF 추가
]

//=========================================
// collect etf holdings
//=========================================
List<EtfHolding> allEtfHoldings = []
etfSymbols.each{
    def holdings = getEtfHoldings(it)
    allEtfHoldings.addAll(holdings)
}
println "allEtfHoldings:${allEtfHoldings}"

//=========================================
// distinct sum of weight
//=========================================
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
        return null
    }
    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return null
    }
    // set marketCap
    it.marketCap = asset.getMarketCap()
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
