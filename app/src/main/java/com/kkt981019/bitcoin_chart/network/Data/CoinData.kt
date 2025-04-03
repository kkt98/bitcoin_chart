package com.kkt981019.bitcoin_chart.network.Data

data class CoinData(
    val koreanName: String,        // 한글명
    val englishName:String,     //영어명
    val symbol: String,      // 예: BTC/USDT
    val tradePrice: Double?,    // 현재가
    val changeRate: Double?,  // 전일대비(%, +면 상승, -면 하락)
    val volume: Double?,       // 거래대금
    val signed: Double?,         // 부호가 있는 변화액
    val change: String,         // 전일대비 가격 상승 하락 동일
)
