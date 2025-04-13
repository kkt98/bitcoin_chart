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
    @SerializedName("ask_price") val askPrice: Double,
    @SerializedName("bid_price") val bidPrice: Double,
    @SerializedName("ask_size") val askSize: Double,
    @SerializedName("bid_size") val bidSize: Double
)
