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
    @GET("/v1/candles/days")
    suspend fun getDayCandles(
        @Query("market") market: String,    // ex: "KRW-AERGO"
        @Query("count") count: Int = 200
    ): List<RetrofitCandleResponse>

    //분별 캔들 조회
    @GET("/v1/candles/minutes/{unit}")
    suspend fun getMinuteCandles(
        @Path("unit") unit: Int,          // 1분 단위
        @Query("market") market: String,      // ex: "KRW-BTC"
        @Query("count") count: Int = 200        // 몇 개 데이터를 가져올 건지
    ): List<RetrofitCandleResponse>

}