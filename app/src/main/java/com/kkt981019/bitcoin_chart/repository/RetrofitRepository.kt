package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
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

}