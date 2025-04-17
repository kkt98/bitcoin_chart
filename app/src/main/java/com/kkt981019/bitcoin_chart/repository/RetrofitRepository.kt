package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
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
        // Response<> 없이 바로 List를 반환하므로 isSuccessful 체크 불필요
        return api.getTrade(market = market)
    }

}