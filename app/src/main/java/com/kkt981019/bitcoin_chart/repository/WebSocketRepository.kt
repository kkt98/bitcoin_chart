package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.network.websoketlistener.TickerWebSocketListener
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import com.kkt981019.bitcoin_chart.network.websoketlistener.CandleWebSocketListener
import com.kkt981019.bitcoin_chart.network.websoketlistener.OrderBookWebSocketListener
import com.kkt981019.bitcoin_chart.network.websoketlistener.TradeWebSocketListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository @Inject constructor() {

    private val client = OkHttpClient()

    // 기존 MainScreen 전용
    fun startTickerSocket(
        marketCodes: List<String>?,
        onTickerUpdate: (WebsocketResponse) -> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()

        val listener = TickerWebSocketListener(marketCodes, onTickerUpdate)
        return client.newWebSocket(request, listener)
    }

    fun startTradeSocket(
        marketCode: String,
        onTradeUpdate: (WebSocketTradeResponse)-> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()
        val listener = TradeWebSocketListener(marketCode, onTradeUpdate)
        return client.newWebSocket(request, listener)
    }

    fun startOrderBookSocket(
        marketCode: String,
        onOrderbookUpdate: (OrderbookResponse) -> Unit,
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()
        val listener = OrderBookWebSocketListener(marketCode, onOrderbookUpdate)
        return client.newWebSocket(request, listener)
    }

    fun starCandleSocket(
        marketCode: String,
        onCandleUpdate: (WebSocketCandleResponse)-> Unit
    ): WebSocket {
        val request = Request.Builder()
            .url("wss://api.upbit.com/websocket/v1")
            .build()
        val listener = CandleWebSocketListener(marketCode, onCandleUpdate)
        return client.newWebSocket(request, listener)
    }
}