package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitResponse
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

}