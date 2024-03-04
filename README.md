# fintics
Fintics

하지 마세요.
잘못하면 신세 조집니다....

- positive : > 70
- nuetral : 50
- negative: < 50

class Analysis(List<Ohlcv> ohlcvs) 

	def getMomentumScore 

	def getOverboughtScore

	def getOversoldScore


def fastAnalysis = new Analysis(ohlcvsM1)

def slowAnalysis = new Analysis(ohlcvsM5)

def marketAnalysises = [
	hourly: new Analysis(ohlcvM60)
	daily: new Analysis(ohlcvD1)
	kospi: new Analysis(indiceIndicator['KOSPI'].getOhlcvs(Ohlcv.Type.DAILY,1)
]











