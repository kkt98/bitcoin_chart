package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import com.kkt981019.bitcoin_chart.network.UpbitApi
import javax.inject.Inject

class RetrofitRepository @Inject constructor(private val api: UpbitApi) {

    suspend fun getAllMarket(): List<MarketResponse>? {
        val marketResponse = api.getMarkets()

        if (marketResponse.isSuccessful) {
            val krwMarkets = marketResponse.body()

            return krwMarkets
        }

        return emptyList()
    }

    suspend fun getAllPrice(markets: List<String>?): List<WebsocketResponse>? {
        val marketParam = markets?.joinToString(separator = ",")

        val response = api.getTicker(marketParam)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun getTrade(market: String): List<RetrofitTradeResponse> {
        return api.getTrade(market = market)
    }

    suspend fun getDayCandle(
        market: String,
        count: Int = 100,
        to: String? = null
    ): List<RetrofitCandleResponse> {
        return api.getDayCandles(
            market = market,
            count = count,
            to    = to
        )
    }

    suspend fun getMinuteCandle(
        market: String,
        minute: Int,
        count: Int = 100,
        to: String? = null
    ): List<RetrofitCandleResponse> {
        return api.getMinuteCandles(
            unit  = minute,
            market = market,
            count = count,
            to    = to
        )
    }

}