package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.CoinWebSocketListener
import com.kkt981019.bitcoin_chart.network.Data.TickerResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository @Inject constructor() {

    private val client = OkHttpClient()

    fun startSocket(
        marketCodes: List<String>,
        onTickerUpdate: (TickerResponse) -> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")  // Upbit WebSocket 엔드포인트
            .build()

        val listener = CoinWebSocketListener(marketCodes, onTickerUpdate)
        return client.newWebSocket(request, listener)
    }
}