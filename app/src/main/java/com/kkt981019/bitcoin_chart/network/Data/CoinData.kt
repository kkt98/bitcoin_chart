package com.kkt981019.bitcoin_chart.network.Data

data class CoinData(
    val koreanName: String,        // 한글명
    val englishName:String,
    val symbol: String,      // 예: BTC/USDT
    val currentPrice: String,
    val tradePrice: Double?,    // 현재가
    val changeRate: Double?,  // 전일대비(%, +면 상승, -면 하락)
    val volume: Double?       // 거래대금
)
