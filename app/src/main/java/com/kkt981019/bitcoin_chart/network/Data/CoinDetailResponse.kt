package com.kkt981019.bitcoin_chart.network.Data

import com.google.gson.annotations.SerializedName

data class CoinDetailResponse(
    @SerializedName("trade_price") val trade_price: String,
    @SerializedName("signed_change_rate") val signed_change_rate: String,
    @SerializedName("signed_change_price") val signed_change_price: String,
    @SerializedName("change") val change: String,
)

data class OrderbookResponse(
    val orderbook_units: List<OrderbookUnit>
)

data class OrderbookUnit(
    val ask_price: Double,  // 매도 호가
    val bid_price: Double,  // 매수 호가
    val ask_size: Double,   // 매도 잔량
    val bid_size: Double    // 매수 잔량
)
