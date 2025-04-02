package com.kkt981019.bitcoin_chart.network.Data

import com.google.gson.annotations.SerializedName

data class TickerResponse(
    @SerializedName("market") val market: String,
    @SerializedName("trade_price") val trade_price: String,
    @SerializedName("signed_change_rate") val signed_change_rate: String,
    @SerializedName("acc_trade_price_24h") val acc_trade_price_24h: String,
    @SerializedName("signed_change_price") val signed_change_price: String,
    )
