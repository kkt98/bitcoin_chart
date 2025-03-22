package com.kkt981019.bitcoin_chart.network.Data

import com.google.gson.annotations.SerializedName

data class TickerResponse(
    @SerializedName("market") val market: String,
    @SerializedName("trade_price") val tradePrice: Double
)
