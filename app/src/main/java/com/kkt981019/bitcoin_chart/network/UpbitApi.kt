package com.kkt981019.bitcoin_chart.network

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UpbitApi {

    // 마켓 목록 조회 (모든 거래 마켓)
    @GET("v1/market/all")
    suspend fun getMarkets(
        @Query("isDetails") isDetails: Boolean = false
    ): Response<List<MarketResponse>>

    // 티커 조회 (여러 마켓의 시세 정보)
    @GET("v1/ticker")
    suspend fun getTicker(
        @Query("markets") markets: String?
    ): Response<List<WebsocketResponse>>

}