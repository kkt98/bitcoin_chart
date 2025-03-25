package com.kkt981019.bitcoin_chart.network.Data

import com.google.gson.annotations.SerializedName

data class TickerResponse(
    @SerializedName("signed_change_price") val market: String,
    @SerializedName("signed_change_rate") val tradePrice1: String,
    @SerializedName("acc_trade_price_24h") val tradePrice2: String
)
