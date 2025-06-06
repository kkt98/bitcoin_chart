package com.kkt981019.bitcoin_chart.network

import com.kkt981019.bitcoin_chart.network.Data.MarketResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    // Trade 조회 (여러 마켓의 시세 정보)
    @GET("v1/trades/ticks")
    suspend fun getTrade(
        @Query("market") market: String,
        @Query("count") count: Int = 100
    ): List<RetrofitTradeResponse>

    // 일별 캔들 조회
    @GET("v1/candles/days")
    suspend fun getDayCandles(
        @Query("market") market: String,    // ex: "KRW-BTC"
        @Query("count") count: Int = 100,
        @Query("to") to: String? = null   // 이 ISO 8601 시각 이전 데이터
    ): List<RetrofitCandleResponse>

    // 분·시간 단위 캔들 (unit 분봉)
    @GET("v1/candles/minutes/{unit}")
    suspend fun getMinuteCandles(
        @Path("unit") unit: Int,         // 1,3,5,15,30,60,240
        @Query("market") market: String,  // ex: "KRW-BTC"
        @Query("count") count: Int = 100, // 최대 200개
        @Query("to") to: String? = null   // 이 ISO 8601 시각 이전 데이터
    ): List<RetrofitCandleResponse>

}