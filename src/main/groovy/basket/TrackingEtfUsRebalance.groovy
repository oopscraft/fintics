import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.*
import groovy.json.JsonSlurper
import org.oopscraft.fintics.basket.BasketChange
import org.oopscraft.fintics.model.*

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
 * gets holdings
 * @param etfSymbol etf symbol
 * @return list of holding
 */
static List<Holding> getHoldings(etfSymbol) {
    def document = Jsoup.connect("https://finance.yahoo.com/quote/${etfSymbol}/holdings/").get()
    def topHoldings= document.select('section[data-testid=top-holdings] .content')
    return topHoldings.collect {
        def symbol = it.select('span.symbol').text()
        def name = it.select('span.name').text()
        def weight = it.select('span.data').text().replace('%','') as BigDecimal
        return Holding.of(symbol, name, weight)
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
List<Holding> allHoldings = []
etfSymbols.each{
    def holdings = getHoldings(it)
    allHoldings.addAll(holdings)
}
println "allHoldings:${allHoldings}"

//=========================================
// distinct sum
//=========================================
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
    Asset asset = assetService.getAsset("US.${it.symbol}").orElse(null)
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


