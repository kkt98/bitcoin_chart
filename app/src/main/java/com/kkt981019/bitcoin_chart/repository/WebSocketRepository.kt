package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.websoketlistener.CoinDetailWebSocketListener
import com.kkt981019.bitcoin_chart.network.websoketlistener.CoinWebSocketListener
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository @Inject constructor() {

    private val client = OkHttpClient()

    // 기존 MainScreen 전용
    fun startSocket(
        marketCodes: List<String>?,
        onTickerUpdate: (WebsocketResponse) -> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()

        val listener = CoinWebSocketListener(marketCodes, onTickerUpdate)
        return client.newWebSocket(request, listener)
    }

    // DetailScreen 전용 WebSocket: 티커와 주문호가를 동시에 받아옴
    fun startDetailSocket(
        marketCode: String,
        onCoinDetailUpdate: (CoinDetailResponse) -> Unit,
        onOrderbookUpdate: (OrderbookResponse) -> Unit,
        onTradeUpdate: (WebSocketTradeResponse) -> Unit,
        onCandleUpdate: (WebSocketCandleResponse)-> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()
        val listener = CoinDetailWebSocketListener(marketCode, onCoinDetailUpdate, onOrderbookUpdate, onTradeUpdate, onCandleUpdate)
        return client.newWebSocket(request, listener)
    }
}